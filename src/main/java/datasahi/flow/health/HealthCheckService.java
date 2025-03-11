package datasahi.flow.health;

import datasahi.flow.ds.DataServer;
import datasahi.flow.ds.DataServerRegistry;
import datasahi.flow.ds.JdbcDataServer;
import datasahi.flow.ds.RedisDataServer;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);

    private final DataServerRegistry dataServerRegistry;

    public HealthCheckService(DataServerRegistry dataServerRegistry) {
        this.dataServerRegistry = dataServerRegistry;
    }

    public HealthSummary performHealthCheck() {
        HealthSummary healthSummary = new HealthSummary();
        for (DataServer ds : dataServerRegistry.getDataServers()) {
            log.info("Performing health check for {}", ds.getId());
            switch (ds.getType()) {
                case REDIS:
                    healthSummary.add(new RedisHealthcheck((RedisDataServer) ds).check());
                    break;
                case JDBC:
                    healthSummary.add(new JdbcHealthcheck((JdbcDataServer) ds).check());
                    break;
                default:
                    healthSummary.add(new HealthResponse().setDataserverId(ds.getId()).setHealthy(false).setMessage("Unsupported data server type: " + ds.getType()));
            }
        }
        return healthSummary;
    }
}
