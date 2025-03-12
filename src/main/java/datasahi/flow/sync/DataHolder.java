package datasahi.flow.sync;

import java.util.ArrayList;
import java.util.List;

public class DataHolder<R> {

    private final String flowId;
    private final BatchInfo batchInfo;

    private final List<DataRecord<R>> records = new ArrayList<>();
    private long systemMillis = System.currentTimeMillis();

    public DataHolder(String flowId, BatchInfo batchInfo) {
        this.flowId = flowId;
        this.batchInfo = batchInfo;
    }

    public void store(DataRecord<R> record) {
        records.add(record);
    }

    public void store(List<DataRecord<R>> recordList) {
        records.addAll(recordList);
    }

    public List<DataRecord<R>> fetch() {
        return records;
    }

    public String getFlowId() {
        return flowId;
    }

    public boolean isBatched() {
        if (records.isEmpty()) return false;
        if (records.size() >= batchInfo.getMaxCount()) return true;
        if ((System.currentTimeMillis() - systemMillis) >= batchInfo.getMaxMillis()) return true;
        return false;
    }

    public int capacity() {
        return batchInfo.getMaxCount() - records.size();
    }

    public void reset() {
        records.clear();
        systemMillis = System.currentTimeMillis();
    }
}
