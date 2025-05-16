package datasahi.flow.ds.smb;

import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.protocol.commons.EnumWithValue;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import datasahi.flow.ds.DataServerType;
import datasahi.flow.health.VerifyRequest;
import datasahi.flow.health.VerifyResponse;
import datasahi.flow.source.DataSource;
import datasahi.flow.sync.DataPipe;
import datasahi.flow.sync.DataRecord;
import datasahi.flow.sync.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SMBSource implements DataSource, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SMBSource.class);

    private final List<DataPipe> dataPipes = new ArrayList<>();
    private final SMBDataServer smbDataServer;

    private SMBClient smbClient;
    private Connection connection;
    private Session session;
    private DiskShare diskShare;

    private final ScheduledExecutorService scheduler;
    private final Map<FileInfo, FileInfo> currentFileMap = new ConcurrentHashMap<>();

    private volatile boolean verifyMode = false;
    private VerifyResponse verifyResponse = null;

    private long pickupMillis = 5000;

    public SMBSource(SMBDataServer dataServer) {
        this.smbDataServer = dataServer;
        this.scheduler = Executors.newScheduledThreadPool(1,
                r -> {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName("SMB-FileWatcher-Thread");
                    return thread;
                });
    }

    @Override
    public String getId() {
        return smbDataServer.getId();
    }

    @Override
    public DataServerType getType() {
        return smbDataServer.getType();
    }

    @Override
    public void addDataPipe(DataPipe dataPipe) {
        dataPipes.add(dataPipe);
    }

    @Override
    public List<DataPipe> getDataPipes() {
        return dataPipes;
    }

    @Override
    public void start() {
        Thread thread = new Thread(this);
        thread.setName("SMBSource-" + smbDataServer.getId());
        thread.start();
    }

    public VerifyResponse verify(VerifyRequest request) {
        this.verifyMode = true;
        this.verifyResponse = new VerifyResponse(request);
        long start = System.currentTimeMillis();

        start();
        while (true) {
            try {
                Thread.sleep(100);
                verifyResponse.setDurationMilis((int) (System.currentTimeMillis() - start));
                if (verifyResponse.isReady()) {
                    stop();
                    break;
                }
            } catch (InterruptedException e) {
                // Nothing to do
            }
        }
        this.verifyMode = false;
        return verifyResponse;
    }

    @Override
    public void run() {
        LOG.info("Starting SMB source " + smbDataServer.getUrl());
        connect();
        startMonitoring(5);
    }

    private void connect() {
        try {
            smbClient = new SMBClient();
            connection = smbClient.connect(smbDataServer.getHost(), smbDataServer.getPort());
            AuthenticationContext authContext = new AuthenticationContext(
                    smbDataServer.getUsername(), smbDataServer.getPassword().toCharArray(), smbDataServer.getDomain());
            session = connection.authenticate(authContext);
            diskShare = (DiskShare) session.connectShare(smbDataServer.getShareName());
        } catch (IOException e) {
            LOG.error("SMB Connection Error", e);
            stop();
            throw new RuntimeException("Error in connecting to SMB share", e);
        }
    }

    private void startMonitoring(int intervalSeconds) {
        scheduler.scheduleWithFixedDelay(this::listFilesRecursively,
                0,
                intervalSeconds,
                TimeUnit.SECONDS);
    }

    private void listFilesRecursively() {
        if (dataPipes.isEmpty()) return;

        try {
            String inputFolder = dataPipes.get(0).getFlow().getSourceDataset().getConfigJson().getString("inputFolder");
            List<FileInfo> newFileList = new ArrayList<>();
            recursiveFileList(diskShare, inputFolder, newFileList, inputFolder);
            List<FileInfo> filesQueued = new ArrayList<>();
            for (FileInfo fileInfo : newFileList) {
                if (currentFileMap.containsKey(fileInfo)) continue;
                currentFileMap.put(fileInfo, fileInfo);
                filesQueued.add(fileInfo);
                DataRecord<FileInfo> record =
                        new DataRecord<>(smbDataServer.getId(), LocalDateTime.now(), fileInfo.getId(), fileInfo);
                for (DataPipe p : dataPipes) {
                    p.addRecord(record);
                }
                if (verifyMode) {
                    verifyResponse.incrementMatched();
                    if (verifyResponse.isReady()) {
                        break;
                    }
                }
            }
            if (!filesQueued.isEmpty()) {
                LOG.info("Total files queued: " + filesQueued.size());
                filesQueued.forEach(fileInfo -> {
                    LOG.info("File queued: " + fileInfo.getFilePath() + " - " + fileInfo.getDateModified());
                });

            } else {
                LOG.debug("Total files queued: " + filesQueued.size());
            }
        } catch (Exception e) {
            LOG.error("Error listing files", e);
        }
    }

    /**
     * Recursive method to list files
     *
     * @param share    DiskShare to explore
     * @param path     Current path to list
     * @param fileList List to populate with FileInfo objects
     */
    private void recursiveFileList(DiskShare share, String path, List<FileInfo> fileList, String rootFolder) {
        LOG.debug("Listing path " + path);
        String folder = path.endsWith("/") ? path : path + "/";
        try {
            List<FileIdBothDirectoryInformation> files = share.list(path);
            LOG.debug("File count for " + path + " is " + files.size());

            for (FileIdBothDirectoryInformation file : files) {
                LOG.debug("File name " + file.getFileName());
                // Skip '.' and '..' entries
                if (file.getFileName().equals(".") || file.getFileName().equals("..")) {
                    continue;
                }
                // Construct full file path
                String fullPath = folder + file.getFileName();
                try {
                    // Create FileInfo object
                    EnumSet<FileAttributes> enumSet = EnumWithValue.EnumUtils.toEnumSet(file.getFileAttributes(), FileAttributes.class);
                    boolean isDirectory = enumSet.contains(FileAttributes.FILE_ATTRIBUTE_DIRECTORY);
                    FileInfo fileInfo = new FileInfo(
                            fullPath,                           // Full file path
                            file.getFileName(),                     // File name
                            isDirectory,                            // Is directory flag
                            file.getEaSize(),                 // File size in bytes
                            file.getCreationTime().toInstant(),
                            file.getLastWriteTime().toInstant()  // Last modified time
                    ).setRelativePath(fullPath.substring(rootFolder.length()));

                    // Recursively list files if it's a directory
                    if (fileInfo.isDirectory()) {
                        recursiveFileList(share, fullPath, fileList, rootFolder);
                    } else {
                        LOG.info(fileInfo.getDateModified().toEpochMilli() + "-" + Instant.now().toEpochMilli());
                        if (fileInfo.getDateModified().isBefore(Instant.now().minusMillis(pickupMillis))) {
                            if (!currentFileMap.containsKey(fileInfo)) {
                                fileList.add(fileInfo);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.warn("Error processing file: " + fullPath, e);
                }
            }
        } catch (Exception e) {
            LOG.error("Error listing files in path: " + path, e);
        }
    }

    private void stopMonitoring() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        stopMonitoring();

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
