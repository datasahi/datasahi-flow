package datasahi.flow.config;

import datasahi.flow.util.FileUtil;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class FlowConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(FlowConfiguration.class);
    private final ConfigService configService;

    private final List<JSONObject> dataServers = new ArrayList<>();
    private final List<JSONObject> flows = new ArrayList<>();

    public FlowConfiguration(ConfigService configService) {
        this.configService = configService;
    }

    @PostConstruct
    public void load() {
        configService.getConfigPaths().forEach(p -> loadConfig(p));
        LOG.info("Loaded {} data servers and {} flows", dataServers.size(), flows.size());
    }

    public List<JSONObject> getDataServers() {
        return dataServers;
    }

    public List<JSONObject> getFlows() {
        return flows;
    }

    private void loadConfig(String path) {
        JSONObject jo = new FileUtil().readJsonFile(path);
        addJsonObjects(jo, dataServers, "dataservers");
        addJsonObjects(jo, flows, "flows");
    }

    private void addJsonObjects(JSONObject jo, List<JSONObject> list, String key) {
        JSONArray dsa = jo.getJSONArray(key);
        for (int i = 0; i < dsa.length(); i++) {
            list.add(dsa.getJSONObject(i));
        }
    }
}
