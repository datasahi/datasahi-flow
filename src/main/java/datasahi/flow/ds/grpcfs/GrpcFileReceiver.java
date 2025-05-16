package datasahi.flow.ds.grpcfs;

import datasahi.flow.ds.DataServerType;
import datasahi.flow.health.VerifyRequest;
import datasahi.flow.health.VerifyResponse;
import datasahi.flow.source.DataSource;
import datasahi.flow.sync.DataPipe;
import datasahi.flow.sync.DataRecord;
import datasahi.flow.sync.FileInfo;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GrpcFileReceiver implements DataSource, FileInfoReceiver, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GrpcFileReceiver.class);

    private final GrpcFileServer fileServer;

    private final List<DataPipe> dataPipes = new ArrayList<>();
    private Server server;

    private volatile boolean verifyMode = false;
    private VerifyResponse verifyResponse = null;

    public GrpcFileReceiver(GrpcFileServer fileServer) {
        this.fileServer = fileServer;
    }

    @Override
    public String getId() {
        return fileServer.getId();
    }

    @Override
    public DataServerType getType() {
        return fileServer.getType();
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
    public void start() {
        Thread thread = new Thread(this);
        thread.setName("GrpcFileReceiver-" + fileServer.getId());
        thread.start();
    }

    @Override
    public void run() {
        try {
            this.server = ServerBuilder.forPort(fileServer.getPort())
                    .addService(new FileTransferServiceImpl(fileServer.getWorkFolder(), this))
                    .build()
                    .start();
            blockUntilShutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("File Transfer Server started, listening on " + fileServer.getPort());
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
        server = null;
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    @Override
    public void receiveFileInfo(FileInfo fileInfo) {
        DataRecord<FileInfo> record =
                new DataRecord<>(fileServer.getId(), LocalDateTime.now(), fileInfo.getId(), fileInfo);
        for (DataPipe p : dataPipes) {
            p.addRecord(record);
        }
        if (verifyMode) {
            verifyResponse.incrementMatched();
        }
    }

    @Override
    public void receiveFileInfoError(FileInfo fileInfo, Exception exception) {
        logger.error("Error in receiving file " + fileInfo, exception);
    }
}