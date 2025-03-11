package datasahi.flow.commons.kv;

import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.HashMap;
import java.util.Map;

public class KVStore {

    private final String dbPath;
    private final RocksDB db;

    private final Map<String, ColumnFamilyDescriptor> columnFamilyDescriptors = new HashMap<>();

    public KVStore(String dbPath) {
        this.dbPath = dbPath;
        this.db = init();
    }

    private RocksDB init() {
        RocksDB.loadLibrary();
        try (final Options options = new Options().setCreateIfMissing(true)) {
            return RocksDB.open(options, dbPath);
        } catch (RocksDBException e) {
            throw new RuntimeException("Error in opening rocksdb at " + dbPath, e);
        }
    }

    public void close() {
        if (db != null) {
            db.close();
        }
    }

    public void put(String key, byte[] value) {
        if (db == null || db.isClosed()) return;
        try {
            db.put(key.getBytes(), value);
        } catch (RocksDBException e) {
            throw new RuntimeException("Error in fetching value for key " + key, e);
        }
    }

    public byte[] get(String key) {
        if (db == null || db.isClosed()) return null;
        try {
            return db.get(key.getBytes());
        } catch (RocksDBException e) {
            throw new RuntimeException("Error in fetching value for key " + key, e);
        }
    }

    public void delete(String key) {
        if (db == null || db.isClosed()) return;
        try {
            db.delete(key.getBytes());
        } catch (RocksDBException e) {
            throw new RuntimeException("Error in fetching value for key " + key, e);
        }
    }
}
