package datasahi.flow.health;

import datasahi.flow.ds.RedisDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class RedisHealthcheck implements Healthcheck {

    private static final Logger log = LoggerFactory.getLogger(RedisHealthcheck.class);

    private final RedisDataServer redisDataServer;

    public RedisHealthcheck(RedisDataServer redisDataServer) {
        this.redisDataServer = redisDataServer;
    }

    @Override
    public HealthResponse check() {
        try (Jedis jedis = new Jedis(redisDataServer.getUrl())) {
            String pong = jedis.ping();
            if ("PONG".equalsIgnoreCase(pong)) {
                log.info("Redis connection success. {}", redisDataServer);
                return new HealthResponse().setDataserverId(redisDataServer.getId()).setHealthy(true);
            } else {
                log.error("Error connecting to redis, unexpected response: {} for {} ", pong, redisDataServer);
                return new HealthResponse().setDataserverId(redisDataServer.getId()).setHealthy(false).setMessage(pong);
            }
        } catch (Exception e) {
            log.error("Error connecting to Redis server at " + redisDataServer, e);
            return new HealthResponse().setDataserverId(redisDataServer.getId()).setHealthy(false).setMessage(e.getMessage());
        }
    }
}
