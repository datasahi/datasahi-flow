package datasahi.flow.ms;

import datasahi.flow.commons.kv.KVStore;
import datasahi.flow.config.ConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;

@Singleton
public class KVService {

    private final ConfigService configService;
    private KVStore store;

    public KVService(ConfigService configService) {
        this.configService = configService;
    }

    @PostConstruct
    public void init() {
        this.store = new KVStore(configService.getDataDir() + "/kv.db");
    }

    public KVStore getStore() {
        return store;
    }
}
