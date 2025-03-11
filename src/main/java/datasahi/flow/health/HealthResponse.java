package datasahi.flow.health;

import java.time.LocalDateTime;

public class HealthResponse {

    private String dataserverId;
    private boolean healthy;
    private String message;
    private LocalDateTime doneAt = LocalDateTime.now();

    public String getDataserverId() {
        return dataserverId;
    }

    public HealthResponse setDataserverId(String dataserverId) {
        this.dataserverId = dataserverId;
        return this;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public HealthResponse setHealthy(boolean healthy) {
        this.healthy = healthy;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public HealthResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public LocalDateTime getDoneAt() {
        return doneAt;
    }

    public HealthResponse setDoneAt(LocalDateTime doneAt) {
        this.doneAt = doneAt;
        return this;
    }


    @Override
    public String toString() {
        return "HealthResponse{" +
                "healthy=" + healthy +
                ", message='" + message + '\'' +
                ", doneAt=" + doneAt +
                '}';
    }
}
