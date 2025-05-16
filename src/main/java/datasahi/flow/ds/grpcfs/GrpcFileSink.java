package datasahi.flow.ds.grpcfs;

import datasahi.flow.ds.DataServerRegistry;
import datasahi.flow.ds.DataServerType;
import datasahi.flow.ds.smb.SMBDataServer;
import datasahi.flow.ds.smb.SMBFileClient;
import datasahi.flow.sink.DataSink;
import datasahi.flow.sync.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GrpcFileSink implements DataSink {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcFileSink.class);

    private final GrpcFileServer fileServer;
    private final DataServerRegistry dataServerRegistry;

    private final Map<String, Flow> flows = new HashMap<>();
    private SMBFileClient smbFileClient;
    private GrpcFileClient grpcFileClient;

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    public GrpcFileSink(GrpcFileServer fileServer, DataServerRegistry dataServerRegistry) {
        this.fileServer = fileServer;
        this.dataServerRegistry = dataServerRegistry;
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
    public void process(DataHolder dataHolder) {
        //Nothing to do
    }

    @Override
    public void processBatch(DataHolder dataHolder) {

        List<DataRecord<FileInfo>> records = dataHolder.fetch();
        for (DataRecord<FileInfo> record : records) {
            FileInfo fileInfo = record.getRecord();
            GrpcFileSender grpcFileSender = new GrpcFileSender(smbFileClient, grpcFileClient, fileInfo);
            executor.submit(grpcFileSender);
        }
    }

    @Override
    public void start() {
        if (flows.isEmpty()) return;
        Flow flow = getFlows().get(0);
        SMBDataServer smbServer = (SMBDataServer) dataServerRegistry.get(flow.getSourceId());
        if (smbServer == null) {
            throw new IllegalStateException("SMB Data Server not found for flow: " + flow.getId());
        }
        smbFileClient = new SMBFileClient(smbServer, flow.getSourceDataset().getConfigJson());
        smbFileClient.connect();

        grpcFileClient = new GrpcFileClient(fileServer.getHost(), fileServer.getPort());
    }

    @Override
    public void stop() {

    }

    @Override
    public void addFlow(Flow flow) {
        flows.put(flow.getId(), flow);
    }

    @Override
    public List<Flow> getFlows() {
        return new ArrayList<>(flows.values());
    }

}
