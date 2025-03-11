package datasahi.flow.sync;

import datasahi.flow.ds.DataServerType;

import java.util.List;

public interface DataSink {

    String getId();
    DataServerType getType();
    void process(DataHolder dataHolder);
    void processBatch(DataHolder dataHolder);
    void addSubscription(Subscription subscription);
    List<Subscription> getSubscriptions();
    void start();
    void stop();
}
