package datasahi.flow.config;

import datasahi.flow.ds.DataServerRegistry;
import datasahi.flow.sync.SubscriptionRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RegistryMaker {

    private static final Logger LOG = LoggerFactory.getLogger(RegistryMaker.class);

    private final DataServerRegistry dataServerRegistry;
    private final SubscriptionRegistry subscriptionRegistry;
    private final XferConfiguration xferConfiguration;

    public RegistryMaker(DataServerRegistry dataServerRegistry, SubscriptionRegistry subscriptionRegistry,
                         XferConfiguration xferConfiguration) {
        this.dataServerRegistry = dataServerRegistry;
        this.subscriptionRegistry = subscriptionRegistry;
        this.xferConfiguration = xferConfiguration;
    }

    @PostConstruct
    public void load() {
        LOG.info("Loading data servers and subscriptions");
        xferConfiguration.getDataServers().forEach(ds -> dataServerRegistry.register(ds));
        xferConfiguration.getSubscriptions().forEach(ds -> subscriptionRegistry.register(ds));
    }

    public DataServerRegistry getDataServerRegistry() {
        return dataServerRegistry;
    }

    public SubscriptionRegistry getSubscriptionRegistry() {
        return subscriptionRegistry;
    }

    public XferConfiguration getXferConfiguration() {
        return xferConfiguration;
    }
}
