package datasahi.flow.ds;

import com.google.gson.Gson;
import jakarta.inject.Singleton;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DataServerRegistry {

    private Map<String, DataServer> dsMap = new ConcurrentHashMap<>();
    private Gson gson = new Gson();

    public void register(DataServer ds) {
        dsMap.put(ds.getId(), ds);
    }

    public void register(JSONObject dsJson) {

        String type = dsJson.getString("type");
        DataServer ds = null;
        switch(DataServerType.valueOf(type)) {
            case REDIS:
                ds = gson.fromJson(dsJson.toString(), RedisDataServer.class);
                break;
            case JDBC:
                ds = gson.fromJson(dsJson.toString(), JdbcDataServer.class);
                break;
            default:
                throw new IllegalArgumentException("Unsupported data server type: " + type);
        }
        register(ds);
    }

    public DataServer get(String id) {
        return dsMap.get(id);
    }

    public List<DataServer> getDataServers() {
        return List.copyOf(dsMap.values());
    }
}
