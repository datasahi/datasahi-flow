package datasahi.flow.ds.grpcfs;

import datasahi.flow.sync.FileInfo;

public interface FileInfoReceiver {
    void receiveFileInfo(FileInfo fileInfo);
    void receiveFileInfoError(FileInfo fileInfo, Exception exception);
}
