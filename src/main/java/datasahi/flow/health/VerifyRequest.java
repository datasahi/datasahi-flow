package datasahi.flow.health;

public class VerifyRequest {

    private final String flowId;
    private final int maxRecordCount;
    private final int maxSeconds;

    public VerifyRequest(String flowId, int maxRecordCount, int maxSeconds) {
        this.flowId = flowId;
        this.maxRecordCount = maxRecordCount;
        this.maxSeconds = maxSeconds;
    }

    public String getFlowId() {
        return flowId;
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
                "flowId='" + flowId + '\'' +
                ", maxRecordCount=" + maxRecordCount +
                ", maxSeconds=" + maxSeconds +
                '}';
    }
}
