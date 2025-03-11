package datasahi.flow.checksum;

import datasahi.flow.ms.KVService;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Singleton
public class ChecksumService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChecksumService.class);

    private final KVService kvService;

    public ChecksumService(KVService kvService) {
        this.kvService = kvService;
    }

    public String getChecksum(String group, Map<String, String> record) {
        return "checksum";
    }
}
