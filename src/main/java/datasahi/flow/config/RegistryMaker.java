package datasahi.flow.config;

import datasahi.flow.ds.DataServerRegistry;
import datasahi.flow.sync.FlowRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RegistryMaker {

    private static final Logger LOG = LoggerFactory.getLogger(RegistryMaker.class);

    private final DataServerRegistry dataServerRegistry;
    private final FlowRegistry flowRegistry;
    private final FlowConfiguration flowConfiguration;

    public RegistryMaker(DataServerRegistry dataServerRegistry, FlowRegistry flowRegistry,
                         FlowConfiguration flowConfiguration) {
        this.dataServerRegistry = dataServerRegistry;
        this.flowRegistry = flowRegistry;
        this.flowConfiguration = flowConfiguration;
    }

    @PostConstruct
    public void load() {
        LOG.info("Loading data servers and flows");
        flowConfiguration.getDataServers().forEach(ds -> dataServerRegistry.register(ds));
        flowConfiguration.getFlows().forEach(ds -> flowRegistry.register(ds));
    }

    public DataServerRegistry getDataServerRegistry() {
        return dataServerRegistry;
    }

    public FlowRegistry getFlowRegistry() {
        return flowRegistry;
    }

    public FlowConfiguration getFlowConfiguration() {
        return flowConfiguration;
    }
}
