package datasahi.flow.sink;

import datasahi.flow.ds.DataServerType;
import datasahi.flow.sync.DataHolder;
import datasahi.flow.sync.Flow;

import java.util.List;

public interface DataSink {

    String getId();
    DataServerType getType();
    void process(DataHolder dataHolder);
    void processBatch(DataHolder dataHolder);
    void addFlow(Flow flow);
    List<Flow> getFlows();
    void start();
    void stop();
}
