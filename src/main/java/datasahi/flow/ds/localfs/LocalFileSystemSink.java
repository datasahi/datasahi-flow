package datasahi.flow.ds.localfs;

import datasahi.flow.ds.DataServerRegistry;
import datasahi.flow.ds.DataServerType;
import datasahi.flow.sink.DataSink;
import datasahi.flow.sync.DataHolder;
import datasahi.flow.sync.DataRecord;
import datasahi.flow.sync.FileInfo;
import datasahi.flow.sync.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalFileSystemSink implements DataSink {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileSystemSink.class);

    private final LocalFileServer fileServer;
    private final DataServerRegistry dataServerRegistry;

    private final Map<String, Flow> flows = new HashMap<>();

    private volatile boolean operational = false;

    public LocalFileSystemSink(LocalFileServer fileServer, DataServerRegistry dataServerRegistry) {
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
        moveFiles(dataHolder);
    }

    @Override
    public void processBatch(DataHolder dataHolder) {
        moveFiles(dataHolder);
    }

    private void moveFiles(DataHolder dataHolder) {

        if (!operational) {
            LOG.warn("Ignoring the data holder as the sink is not operational");
            return;
        }

        List<DataRecord<FileInfo>> records = dataHolder.fetch();
        for (DataRecord<FileInfo> record : records) {
            FileInfo fileInfo = record.getRecord();
            String sourcePath = fileInfo.getFilePath();
            boolean slashNeeded = fileInfo.getRelativePath().charAt(0) != '/';
            String targetPath = fileServer.getDataFolder() + (slashNeeded ? "/" : "") + fileInfo.getRelativePath();
            try {
                Files.move(Path.of(sourcePath), Path.of(targetPath));
            } catch (IOException e) {
                throw new RuntimeException("Error in moving file from " + sourcePath + " to " + targetPath, e);
            }
        }
    }

    @Override
    public void start() {
        operational = true;
    }

    @Override
    public void stop() {
        operational = false;
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
