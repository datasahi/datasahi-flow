package datasahi.flow.health;

import java.util.ArrayList;
import java.util.List;

public class HealthSummary {

    private int total;
    private int healthy;
    private int notHealthy;
    private final List<HealthResponse> healthResponses = new ArrayList<>();

    public void add(HealthResponse response) {
        healthResponses.add(response);
        total++;
        if (response.isHealthy()) {
            healthy++;
        } else {
            notHealthy++;
        }
    }

    public int getTotal() {
        return total;
    }

    public int getHealthy() {
        return healthy;
    }

    public int getNotHealthy() {
        return notHealthy;
    }

    public List<HealthResponse> getHealthResponses() {
        return healthResponses;
    }
}
