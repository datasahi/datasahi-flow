package datasahi.flow.ms;

import datasahi.flow.config.ConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.concurrent.ConcurrentMap;

@Singleton
public class MapDBService {

    private final String dbFile;
    private DB db;

    public MapDBService(ConfigService configService) {
        this.dbFile = configService.getDataDir() + "/milestones.db";
    }

    @PostConstruct
    public void init() {
        //checkDBIsOpen();
    }

    public void store(String group, String key, Object value) {
        checkDBIsOpen();
        ConcurrentMap map = db.hashMap(group).createOrOpen();
        map.put(key, value);
        db.commit();
        db.close();
    }

    private void checkDBIsOpen() {
        if (this.db == null || this.db.isClosed()) {
            this.db = DBMaker.fileDB(this.dbFile).make();
        }
    }

    public Object fetch(String group, String key) {
        checkDBIsOpen();
        ConcurrentMap map = db.hashMap(group).createOrOpen();
        Object value = map.get(key);
        db.close();
        return value;
    }

    public String getDbFile() {
        return dbFile;
    }
}
