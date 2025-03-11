package datasahi.flow.sync;

import java.time.LocalDateTime;

public class DataRecord<T> {

    private final String sourceId;
    private final LocalDateTime timestamp;
    private final String id;
    private final T record;

    public DataRecord(String sourceId, LocalDateTime timestamp, String id, T record) {
        this.sourceId = sourceId;
        this.timestamp = timestamp;
        this.id = id;
        this.record = record;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getId() {
        return id;
    }

    public T getRecord() {
        return record;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
