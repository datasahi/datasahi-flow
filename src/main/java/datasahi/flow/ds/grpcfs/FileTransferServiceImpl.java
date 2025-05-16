package datasahi.flow.ds.grpcfs;

import datasahi.flow.sync.FileInfo;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

public class FileTransferServiceImpl extends FileTransferServiceGrpc.FileTransferServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GrpcFileReceiver.class);

    private final String workFolder;
    private final FileInfoReceiver fileInfoReceiver;

    public FileTransferServiceImpl(String workFolder, FileInfoReceiver fileInfoReceiver) {
        this.workFolder = workFolder;
        this.fileInfoReceiver = fileInfoReceiver;
    }

    @Override
    public StreamObserver<FileChunk> receiveFile(StreamObserver<FileReceiveResponse> responseObserver) {
        return new StreamObserver<FileChunk>() {
            private BufferedOutputStream bos;
            private FileInfo fileInfo;
            private boolean errorOccurred = false;

            @Override
            public void onNext(FileChunk chunk) {
                try {
                    // First chunk - initialize file output stream
                    if (bos == null) {
                        boolean slashRequired = chunk.getFilePath().charAt(0) == '/';
                        String currentFilePath = workFolder + (slashRequired ? "/" : "") + chunk.getFilePath();
                        fileInfo = new FileInfo(currentFilePath, chunk.getFileName(), false,
                                chunk.getSizeInBytes(), Instant.ofEpochMilli(chunk.getDateCreated()),
                                Instant.ofEpochMilli(chunk.getDateModified()))
                                .setRelativePath(chunk.getFilePath());

                        // Ensure directory exists
                        Files.createDirectories(Paths.get(fileInfo.getFilePath()).getParent());

                        bos = new BufferedOutputStream(new FileOutputStream(fileInfo.getFilePath()));
                        logger.info("Starting to receive file: " + fileInfo.getFilePath());
                    }

                    // Write chunk to file
                    bos.write(chunk.getContent().toByteArray());

                    // If last chunk, close the stream
                    if (chunk.getIsLastChunk()) {
                        bos.close();
                        logger.info("File received successfully: " + fileInfo.getFilePath());
                        fileInfoReceiver.receiveFileInfo(fileInfo);
                        fileInfo = null;
                        bos = null;
                    }
                } catch (IOException e) {
                    errorOccurred = true;
                    fileInfoReceiver.receiveFileInfoError(fileInfo, e);
                }
            }

            @Override
            public void onError(Throwable t) {
                errorOccurred = true;
                fileInfoReceiver.receiveFileInfoError(fileInfo, new RuntimeException(t));
                // Close the stream if it's open
                try {
                    if (bos != null) {
                        bos.close();
                    }
                } catch (IOException e) {
                    logger.warn("Error closing file stream", e);
                }

                // Send error response
                responseObserver.onNext(FileReceiveResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("File transfer failed: " + t.getMessage())
                        .build());
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                // Send success response if no errors occurred
                FileReceiveResponse response = FileReceiveResponse.newBuilder()
                        .setSuccess(!errorOccurred)
                        .setMessage(errorOccurred ? "File transfer encountered errors" : "File transferred successfully")
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }
}
