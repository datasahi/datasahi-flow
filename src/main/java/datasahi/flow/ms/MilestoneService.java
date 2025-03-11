package datasahi.flow.ms;

import datasahi.flow.util.LocalDateTimeConverter;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Singleton
public class MilestoneService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MilestoneService.class);

    private final KVService kvService;

    public MilestoneService(KVService kvService) {
        this.kvService = kvService;
    }

    public void store(String key, LocalDateTime value) {
        String mkey = "milestones:" + key;
        LocalDateTime last = fetch(mkey);
        if (last == null || value.isAfter(last)) {
            kvService.getStore().put(mkey, LocalDateTimeConverter.toBytes(value));
        }
    }

    public LocalDateTime fetch(String key) {
        byte[] bytes = kvService.getStore().get("milestones:" + key);
        if (bytes == null) return null;
        LocalDateTime localDateTime = LocalDateTimeConverter.fromBytes(bytes);
        LOGGER.info("Fetched milestone {} for key {}", localDateTime, key);
        return localDateTime;
    }
}
