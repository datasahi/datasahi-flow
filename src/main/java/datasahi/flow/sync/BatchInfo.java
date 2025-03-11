package datasahi.flow.sync;

public class BatchInfo {

    private int maxCount;
    private int maxMillis;

    public BatchInfo() {
        this(1,1);
    }

    public BatchInfo(int maxCount, int maxMillis) {
        this.maxCount = maxCount;
        this.maxMillis = maxMillis;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int getMaxMillis() {
        return maxMillis;
    }

    public BatchInfo setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public BatchInfo setMaxMillis(int maxMillis) {
        this.maxMillis = maxMillis;
        return this;
    }

    @Override
    public String toString() {
        return "BatchInfo{" +
                "maxCount=" + maxCount +
                ", maxMillis=" + maxMillis +
                '}';
    }
}
