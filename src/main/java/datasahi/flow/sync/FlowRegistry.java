package datasahi.flow.sync;

import jakarta.inject.Singleton;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class FlowRegistry {

    private Map<String, Flow> flowMap = new ConcurrentHashMap<>();

    public void register(Flow sub) {
        flowMap.put(sub.getId(), sub);
    }

    public List<Flow> getFlows() {
        return flowMap.values().stream().toList();
    }

    public void register(JSONObject dsJson) {
        Flow flow = new Flow(dsJson);
        register(flow);
    }

    public Flow get(String id) {
        return flowMap.get(id);
    }
}
