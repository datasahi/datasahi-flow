package datasahi.flow.ds.grpcfs;

import datasahi.flow.ds.smb.SMBFileClient;
import datasahi.flow.sync.FileInfo;

import java.util.concurrent.Callable;

public class GrpcFileSender implements Callable<Void> {

    private final SMBFileClient fileClient;
    private final GrpcFileClient grpcFileClient;
    private final FileInfo fileInfo;

    public GrpcFileSender(SMBFileClient fileClient, GrpcFileClient grpcFileClient, FileInfo fileInfo) {
        this.fileClient = fileClient;
        this.grpcFileClient = grpcFileClient;
        this.fileInfo = fileInfo;
    }

    @Override
    public Void call() {
        try {
            if (!fileClient.fileExists(fileInfo.getFilePath())) return null;
            byte[] fileBytes = fileClient.getFileBytes(fileInfo.getFilePath());
            grpcFileClient.send(fileInfo, fileBytes);
            fileClient.toArchive(fileInfo);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                fileClient.toError(fileInfo);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
