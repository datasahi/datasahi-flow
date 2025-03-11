package datasahi.flow.sync;

import jakarta.inject.Singleton;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SubscriptionRegistry {

    private Map<String, Subscription> sMap = new ConcurrentHashMap<>();

    public void register(Subscription sub) {
        sMap.put(sub.getId(), sub);
    }

    public List<Subscription> getSubscriptions() {
        return sMap.values().stream().toList();
    }

    public void register(JSONObject dsJson) {
        Subscription subscription = new Subscription(dsJson);
        register(subscription);
    }

    public Subscription get(String id) {
        return sMap.get(id);
    }
}
