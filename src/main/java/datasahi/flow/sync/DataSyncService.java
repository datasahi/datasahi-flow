package datasahi.flow.sync;

import datasahi.flow.config.RegistryMaker;
import datasahi.flow.ds.DataServer;
import datasahi.flow.ds.DataServerType;
import datasahi.flow.ds.JdbcDataServer;
import datasahi.flow.ds.RedisDataServer;
import datasahi.flow.ds.grpcfs.GrpcFileReceiver;
import datasahi.flow.ds.grpcfs.GrpcFileServer;
import datasahi.flow.ds.grpcfs.GrpcFileSink;
import datasahi.flow.ds.localfs.LocalFileServer;
import datasahi.flow.ds.localfs.LocalFileSystemSink;
import datasahi.flow.ds.smb.SMBDataServer;
import datasahi.flow.ds.smb.SMBSource;
import datasahi.flow.health.VerifyRequest;
import datasahi.flow.health.VerifyResponse;
import datasahi.flow.ms.MilestoneService;
import datasahi.flow.sink.DataSink;
import datasahi.flow.sink.JdbcSink;
import datasahi.flow.source.DataSource;
import datasahi.flow.source.RedisSource;
import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Context
@Singleton
public class DataSyncService {

    private static final Logger LOG = LoggerFactory.getLogger(DataSyncService.class);

    private final RegistryMaker registryMaker;
    private final MilestoneService milestoneService;

    private final Map<String, DataSource> sourceMap = new ConcurrentHashMap<>();
    private final Map<String, DataSink> sinkMap = new ConcurrentHashMap<>();
    private final Map<String, DataPipe> pipeMap = new ConcurrentHashMap<>();

    private boolean running = false;

    public DataSyncService(RegistryMaker registryMaker, MilestoneService milestoneService) {
        this.registryMaker = registryMaker;
        this.milestoneService = milestoneService;
    }

    @PostConstruct
    public void init() {
        registryMaker.getFlowRegistry().getFlows().forEach(this::registerSink);
        registryMaker.getFlowRegistry().getFlows().forEach(this::registerSource);
        registryMaker.getFlowRegistry().getFlows().forEach(this::wireFlow);
        LOG.info("Flows setup for data sync. Total flows: " + pipeMap.size());
        registryMaker.getFlowRegistry().getFlows().forEach(f -> LOG.info(f.toString()));
        sinkMap.values().forEach(DataSink::start);
        pipeMap.values().forEach(DataPipe::start);
    }

    public String start() {
        synchronized (this) {
            if (running) {
                return "Sync already started";
            }
            sourceMap.values().forEach(ds -> {
                LOG.info("Starting data source: " + ds.getId());
                ds.start();
                LOG.info("Started data source: " + ds.getId());
            });
            LOG.info("All data sources started");
            running = true;
            return "Sync started";
        }
    }

    public String stop() {
        synchronized (this) {
            if (!running) {
                return "Sync not started";
            }
            sourceMap.values().forEach(ds -> {
                LOG.info("Stopping data source: " + ds.getId());
                ds.stop();
                LOG.info("Stopped data source: " + ds.getId());
            });
            LOG.info("All data sources stopped");
            running = false;
            return "Sync stopped";
        }
    }

    public VerifyResponse verify(VerifyRequest request) {
        synchronized (this) {
            if (running) {
                return new VerifyResponse(request)
                        .setMessage("Sync has started. Please stop the sync and retry this operation");
            }
            Flow flow = registryMaker.getFlowRegistry().get(request.getFlowId());
            String source = flow.getSourceDataset().getServer();
            return sourceMap.get(source).verify(request);
        }
    }

    private void wireFlow(Flow flow) {
        DataSource dataSource = sourceMap.get(flow.getSourceId());
        DataSink dataSink = sinkMap.get(flow.getSinkId());
        DataPipe dataPipe = new DataPipe(flow, dataSink);
        dataSource.addDataPipe(dataPipe);
        dataSink.addFlow(flow);
        pipeMap.put(flow.getId(), dataPipe);
    }

    public void stop(String id) {
        stopPublisher(id);
    }

    public void stopPublisher(String id) {
        if (sourceMap.containsKey(id)) {
            sourceMap.get(id).stop();
        }
    }

    private void registerSource(Flow sub) {
        DataServer source = registryMaker.getDataServerRegistry().get(sub.getSourceId());
        if (source == null) {
            throw new IllegalArgumentException("Source data server not found: " + sub.getSourceId());
        }
        DataServerType type = source.getType();
        switch (type) {
            case REDIS:
                if (!sourceMap.containsKey(sub.getSourceId())) {
                    sourceMap.put(sub.getSourceId(), new RedisSource((RedisDataServer) source, milestoneService));
                }
                break;
            case SMBFS:
                if (!sourceMap.containsKey(sub.getSourceId())) {
                    sourceMap.put(sub.getSourceId(), new SMBSource((SMBDataServer) source));
                }
                break;
            case GRPCFS:
                if (!sourceMap.containsKey(sub.getSourceId())) {
                    sourceMap.put(sub.getSourceId(), new GrpcFileReceiver((GrpcFileServer) source));
                }
                break;
            default:
                throw new RuntimeException(type + " not supported yet for source");
        }
    }

    private void registerSink(Flow sub) {
        DataServer sink = registryMaker.getDataServerRegistry().get(sub.getSinkId());
        if (sink == null) {
            throw new IllegalArgumentException("Sink data server not found: " + sub.getSinkId());
        }
        DataServerType type = sink.getType();
        switch (type) {
            case JDBC:
                if (!sinkMap.containsKey(sub.getSinkId())) {
                    sinkMap.put(sub.getSinkId(), new JdbcSink((JdbcDataServer) sink, milestoneService));
                }
                break;
            case GRPCFS:
                if (!sinkMap.containsKey(sub.getSinkId())) {
                    sinkMap.put(sub.getSinkId(), new GrpcFileSink((GrpcFileServer) sink, registryMaker.getDataServerRegistry()));
                }
                break;
            case LOCALFS:
                if (!sinkMap.containsKey(sub.getSinkId())) {
                    sinkMap.put(sub.getSinkId(), new LocalFileSystemSink((LocalFileServer) sink,
                            registryMaker.getDataServerRegistry()));
                }
                break;
            default:
                throw new RuntimeException(type + " not supported yet for sink");
        }
    }
}
