package datasahi.flow.ds.grpcfs;

import com.google.protobuf.ByteString;
import datasahi.flow.sync.FileInfo;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcFileClient {

    private static final Logger logger = LoggerFactory.getLogger(GrpcFileClient.class);

    private final ManagedChannel channel;
    private final FileTransferServiceGrpc.FileTransferServiceStub asyncStub;

    public GrpcFileClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // Disable TLS for simplicity - use proper security in production
                .build();

        asyncStub = FileTransferServiceGrpc.newStub(channel);
    }

    public void send(FileInfo fileInfo, byte[] fileBytes) {

        StreamObserver<FileReceiveResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(FileReceiveResponse response) {
                logger.info("File transfer response: " +
                        (response.getSuccess() ? "SUCCESS" : "FAILED") +
                        " - " + response.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error in file transfer", t);
            }

            @Override
            public void onCompleted() {
                logger.info("File transfer stream completed");
            }
        };

        StreamObserver<FileChunk> requestObserver = asyncStub.receiveFile(responseObserver);

        try {
            // Chunk size - adjust based on your network and memory constraints
            int chunkSize = 1024 * 1024; // 1MB chunks

            for (int i = 0; i < fileBytes.length; i += chunkSize) {
                int length = Math.min(chunkSize, fileBytes.length - i);
                byte[] chunk = new byte[length];
                System.arraycopy(fileBytes, i, chunk, 0, length);

                FileChunk fileChunk = FileChunk.newBuilder()
                        .setFileId(fileInfo.getId())
                        .setFilePath(fileInfo.getRelativePath())
                        .setFileName(fileInfo.getFileName())
                        .setDateCreated(fileInfo.getDateCreated().toEpochMilli())
                        .setDateModified(fileInfo.getDateModified().toEpochMilli())
                        .setSizeInBytes(fileBytes.length)
                        .setContent(ByteString.copyFrom(chunk))
                        .setIsLastChunk(i + chunkSize >= fileBytes.length)
                        .build();

                requestObserver.onNext(fileChunk);
            }

            requestObserver.onCompleted();
        } catch (Exception e) {
            requestObserver.onError(e);
        }
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown();
    }
}