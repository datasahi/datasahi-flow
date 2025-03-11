package datasahi.flow.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class DataPipe implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DataPipe.class);

    private final Subscription subscription;
    private final DataSink sink;

    private final BlockingQueue<DataRecord> queue;
    private final DataHolder holder;

    public DataPipe(Subscription subscription, DataSink sink) {
        this.queue = new ArrayBlockingQueue<>(subscription.getSourceDataset().getQueueSize());
        this.subscription = subscription;
        this.sink = sink;
        this.holder = new DataHolder(subscription.getId(), subscription.getBatchInfo());
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.setName("DataPipe-" + sink.getId());
        thread.start();
    }

    public void addRecord(DataRecord record) {
        try {
            queue.put(record);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        holder.reset();
        LOG.info("DataPipe started for {}", subscription.getId());
        while (true) {
            try {
                DataRecord record = queue.poll(1, TimeUnit.SECONDS);
                if (record != null) {
//                    LOG.info("received record for key {}", record.getId());
                    holder.store(record);
                    if (holder.capacity() > 0 && queue.size() > 0) {
                        List<DataRecord> records = new ArrayList<>(queue.size());
                        queue.drainTo(records, holder.capacity());
                        holder.store(records);
                        records.clear();
                    }
                }
                if (holder.isBatched()) {
                    long start = System.currentTimeMillis();
                    sink.processBatch(holder);
                    int time = (int) (System.currentTimeMillis() - start);
                    LOG.info("Dataholder processed for record count {} in {} millis", holder.fetch().size(), time);
                    holder.reset();
                }
            } catch (InterruptedException e) {
                // Nothing to do
            } catch (RuntimeException e) {
                LOG.error("Error in processing data for " + sink.getId(), e);
            }
        }
    }
}
