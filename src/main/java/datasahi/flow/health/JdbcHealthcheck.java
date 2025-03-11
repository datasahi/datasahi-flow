package datasahi.flow.health;

import datasahi.flow.commons.db.DatabaseService;
import datasahi.flow.ds.JdbcDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcHealthcheck implements Healthcheck {

    private static final Logger log = LoggerFactory.getLogger(JdbcHealthcheck.class);

    private final JdbcDataServer jdbcDataServer;

    public JdbcHealthcheck(JdbcDataServer jdbcDataServer) {
        this.jdbcDataServer = jdbcDataServer;
    }

    @Override
    public HealthResponse check() {
        DatabaseService databaseService = null;
        try {
            databaseService = new DatabaseService(jdbcDataServer.getDatabaseConfig());
            Object result = databaseService.executeSelectSql("select 1");
            if (result != null && result instanceof Number && ((Number) result).intValue() == 1) {
                log.info("JDBC connection success. {}", jdbcDataServer);
                return new HealthResponse().setDataserverId(jdbcDataServer.getId()).setHealthy(true);
            } else {
                log.error("Error connecting to JDBC, unexpected response: {} for {} ", result, jdbcDataServer);
                String message = (result != null) ? result.toString() : "";
                return new HealthResponse().setDataserverId(jdbcDataServer.getId()).setHealthy(false).setMessage(message);
            }
        } catch (Exception e) {
            log.error("Error connecting to JDBC server at " + jdbcDataServer, e);
            return new HealthResponse().setDataserverId(jdbcDataServer.getId()).setHealthy(false).setMessage(e.getMessage());
        } finally {
            try {
                if (databaseService != null) databaseService.close();
            } catch (Exception e) {
                // Nothing to do
            }
        }
    }
}
