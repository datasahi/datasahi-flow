package datasahi.flow.ds.smb;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import datasahi.flow.sync.FileInfo;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

public class SMBFileClient {

    private static final Logger LOG = LoggerFactory.getLogger(SMBFileClient.class);

    private final SMBDataServer smbDataServer;
    private final JSONObject config;

    private SMBClient smbClient;
    private Connection connection;
    private Session session;
    private DiskShare diskShare;

    public SMBFileClient(SMBDataServer smbDataServer, JSONObject config) {
        this.smbDataServer = smbDataServer;
        this.config = config;
    }

    public boolean fileExists(String filepath) {
        checkOpen();
        return diskShare.fileExists(filepath);
    }

    public void toArchive(FileInfo fileInfo) {
        String source = fileInfo.getFilePath();
        String target = config.getString("archiveFolder") + "/" + fileInfo.getFileName();
        move(source, target);
    }

    public void toError(FileInfo fileInfo) {
        String source = fileInfo.getFilePath();
        String target = config.getString("errorFolder") + "/" + fileInfo.getFileName();
        move(source, target);
    }

    public void move(String source, String target) {
        checkOpen();
        LOG.info("Source : " + source + " to target : " + target);
        File smbFile = diskShare.openFile(
                source,
                EnumSet.of(AccessMask.GENERIC_READ, AccessMask.DELETE),
                null,
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                null
        );

        smbFile.rename(target, true);
    }

    public byte[] getFileBytes(String filepath) {

        checkOpen();

        try {
            // Open the file with read access
            File smbFile = diskShare.openFile(filepath, EnumSet.of(AccessMask.GENERIC_READ), null,
                    SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null
            );
            try (InputStream inputStream = smbFile.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024*1024)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();

            } catch (IOException e) {
                LOG.error("Error reading file bytes: " + filepath, e);
                throw new RuntimeException("Failed to read file bytes", e);
            }
        } catch (Exception e) {
            LOG.error("Error opening SMB file: " + filepath, e);
            throw new RuntimeException("Failed to open SMB file", e);
        }
    }

    private void checkOpen() {
        if (diskShare == null) {
            LOG.error("SMB connection not established. Call connect() first.");
            throw new IllegalStateException("SMB connection not established");
        }
    }

    public void connect() {
        try {
            smbClient = new SMBClient();
            connection = smbClient.connect(smbDataServer.getHost(), smbDataServer.getPort());
            AuthenticationContext authContext = new AuthenticationContext(
                    smbDataServer.getUsername(), smbDataServer.getPassword().toCharArray(), smbDataServer.getDomain());
            session = connection.authenticate(authContext);
            diskShare = (DiskShare) session.connectShare(smbDataServer.getShareName());
        } catch (IOException e) {
            LOG.error("SMB Connection Error", e);
            close();
            throw new RuntimeException("Error in connecting to SMB share", e);
        }

    }

    public void close() {
        try {
            if (diskShare != null) {
                diskShare.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
            if (smbClient != null) {
                smbClient.close();
            }
        } catch (IOException e) {
            LOG.warn("Error closing SMB resources", e);
        }

    }
}
