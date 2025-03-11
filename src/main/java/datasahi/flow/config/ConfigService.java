package datasahi.flow.config;

import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
public class ConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);

    private final Environment environment;

    @Value("${datasahi.xfer.config.paths}")
    private List<String> configPaths;

    @Value("${datasahi.xfer.work.dir}")
    private String workDir;

    public ConfigService(Environment environment) {
        this.environment = environment;
    }

    public List<String> getConfigPaths() {
        return configPaths;
    }

    public String getWorkDir() {
        return workDir;
    }

    public String getDataDir() {
        return workDir + "/data";
    }
}
