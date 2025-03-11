package datasahi.flow.health;

public class VerifyRequest {

    private final String subscriptionId;
    private final int maxRecordCount;
    private final int maxSeconds;

    public VerifyRequest(String subscriptionId, int maxRecordCount, int maxSeconds) {
        this.subscriptionId = subscriptionId;
        this.maxRecordCount = maxRecordCount;
        this.maxSeconds = maxSeconds;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public int getMaxRecordCount() {
        return maxRecordCount;
    }

    public int getMaxSeconds() {
        return maxSeconds;
    }


    @Override
    public String toString() {
        return "VerifyRequest{" +
                "subscriptionId='" + subscriptionId + '\'' +
                ", maxRecordCount=" + maxRecordCount +
                ", maxSeconds=" + maxSeconds +
                '}';
    }
}
