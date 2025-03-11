package datasahi.flow.health;

public class VerifyResponse {

    private final VerifyRequest request;

    private int durationMilis;
    private int matchedRecordCount;
    private int unmatchedRecordCount;

    private String message;

    public VerifyResponse(VerifyRequest request) {
        this.request = request;
    }

    public boolean isReady() {
        return (matchedRecordCount >= request.getMaxRecordCount()) || (durationMilis >= request.getMaxSeconds() * 1000);
    }

    public VerifyRequest getRequest() {
        return request;
    }

    public int getDurationMilis() {
        return durationMilis;
    }

    public int getMatchedRecordCount() {
        return matchedRecordCount;
    }

    public int getUnmatchedRecordCount() {
        return unmatchedRecordCount;
    }

    public VerifyResponse setDurationMilis(int durationMilis) {
        this.durationMilis = durationMilis;
        return this;
    }

    public void incrementMatched() {
        matchedRecordCount++;
    }

    public void incrementUnmatched() {
        unmatchedRecordCount++;
    }

    public String getMessage() {
        return message;
    }

    public VerifyResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return "VerifyResponse{" +
                "request=" + request +
                ", durationMilis=" + durationMilis +
                ", matchedRecordCount=" + matchedRecordCount +
                ", unmatchedRecordCount=" + unmatchedRecordCount +
                '}';
    }
}
