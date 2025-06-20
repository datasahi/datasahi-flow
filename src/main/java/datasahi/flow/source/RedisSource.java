package datasahi.flow.source;

import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.impl.HMSetCommand;
import com.moilioncircle.redis.replicator.cmd.impl.HSetCommand;
import com.moilioncircle.redis.replicator.cmd.impl.PingCommand;
import com.moilioncircle.redis.replicator.cmd.impl.SAddCommand;
import com.moilioncircle.redis.replicator.event.Event;
import com.moilioncircle.redis.replicator.event.PostRdbSyncEvent;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueHash;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueSet;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueString;
import datasahi.flow.ds.DataServerType;
import datasahi.flow.ds.RedisDataServer;
import datasahi.flow.eval.ExpressionEvaluator;
import datasahi.flow.health.VerifyRequest;
import datasahi.flow.health.VerifyResponse;
import datasahi.flow.ms.MilestoneService;
import datasahi.flow.sync.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;

public class RedisSource implements DataSource, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(RedisSource.class);

    private final List<DataPipe> dataPipes = new ArrayList<>();
    private final RedisDataServer redisDataServer;
    private final MilestoneService milestoneService;
    private Replicator replicator;

    private volatile boolean liveTraffic = false;
    private Map<String, LocalDateTime> milestones = new HashMap<>();

    private volatile boolean verifyMode = false;
    private VerifyResponse verifyResponse = null;

    public RedisSource(RedisDataServer dataServer, MilestoneService milestoneService) {
        this.redisDataServer = dataServer;
        this.milestoneService = milestoneService;
    }

    @Override
    public String getId() {
        return redisDataServer.getId();
    }

    @Override
    public DataServerType getType() {
        return redisDataServer.getType();
    }

    @Override
    public void addDataPipe(DataPipe dataPipe) {
        dataPipes.add(dataPipe);
    }

    public List<DataPipe> getDataPipes() {
        return dataPipes;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.setName("RedisSource-" + redisDataServer.getId());
        thread.start();
    }

    public VerifyResponse verify(VerifyRequest request) {
        this.verifyMode = true;
        this.verifyResponse = new VerifyResponse(request);
        long start = System.currentTimeMillis();

        start();
        while (true) {
            try {
                Thread.sleep(100);
                verifyResponse.setDurationMilis((int) (System.currentTimeMillis() - start));
                if (verifyResponse.isReady()) {
                    stop();
                    break;
                }
            } catch (InterruptedException e) {
                // Nothing to do
            }
        }
        this.verifyMode = false;
        return verifyResponse;
    }

    @Override
    public void run() {
        populateMilestones();
        LOG.info("Starting redis source " + redisDataServer.getUrl());
        try {
            stop();
            this.replicator = new RedisReplicator(redisDataServer.getUrl());
            replicator.addEventListener((replicator1, event) -> {
                handleEvent(event);
            });
            replicator.open();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Error in starting redis listener for " + redisDataServer, e);
        }
        LOG.info("Replicator closed " + redisDataServer.getUrl());
    }

    private void populateMilestones() {
        for (DataPipe dataPipe : dataPipes) {
            String sid = dataPipe.getFlow().getId();
            milestones.put(sid, milestoneService.fetch(sid));
        }
    }

    private void handleEvent(Event event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Event :: " + event);
        }
        try {
            if (event instanceof KeyStringValueString) {
                handleStringEvent((KeyStringValueString) event);
            } else if (event instanceof KeyStringValueHash) {
                handleHashEvent((KeyStringValueHash) event);
            } else if (event instanceof KeyStringValueSet) {
                handleSetEvent((KeyStringValueSet) event);
            } else if (event instanceof HMSetCommand) {
                handleHMSetCommand((HMSetCommand) event);
            } else if (event instanceof HSetCommand) {
                handleHSetCommand((HSetCommand) event);
            } else if (event instanceof SAddCommand) {
                handleSAddCommand((SAddCommand) event);
            } else if (event instanceof PostRdbSyncEvent) {
                liveTraffic = true;
                LOG.info("Live traffic started");
            } else if (event instanceof PingCommand) {
                LOG.debug("PingCommand received");
            } else {
                LOG.info("Unhandled event type received: {}", event.getClass().getSimpleName());
            }
        } catch (Exception e) {
            LOG.error("Error handling redis event: " + event, e);
        }
    }

    private void handleSAddCommand(SAddCommand command) {
        String key = new String(command.getKey());
        byte[][] members = command.getMembers();
        Set<String> memberSet = new HashSet<>();
        // Convert and store members
        for (byte[] member : members) {
            String memberStr = new String(member);
            memberSet.add(memberStr);
        }
//        LOG.debug("SAdd set members: {}", memberSet);
        // Add your SAdd command processing logic here
    }

    private void handleHMSetCommand(HMSetCommand command) {
        String key = new String(command.getKey());
        Map<byte[], byte[]> fields = command.getFields();
        Map<String, String> dataHash = new HashMap<>();
        // Convert and store field-value pairs
        fields.forEach((field, value) -> {
            String fieldStr = new String(field);
            String valueStr = new String(value);
            dataHash.put(fieldStr, valueStr);
        });

//        LOG.info("HMSet hash object: {}", dataHash);
        processHashData(key, dataHash);
    }

    private void handleHSetCommand(HSetCommand command) {
        String key = new String(command.getKey());
        Map<byte[], byte[]> fields = command.getFields();
        Map<String, String> dataHash = new HashMap<>();
        // Convert and store field-value pairs
        fields.forEach((field, value) -> {
            String fieldStr = new String(field);
            String valueStr = new String(value);
            dataHash.put(fieldStr, valueStr);
        });

//        LOG.info("HSet hash object: {}", dataHash);
        processHashData(key, dataHash);
    }

    private void handleHashEvent(KeyStringValueHash event) {

        String key = new String(event.getKey());
        Map<byte[], byte[]> hash = event.getValue();
/*
        LOG.debug("Hash event received. Key: {}, Field count: {}, Offsets : {}", key, hash.size(),
                event.getContext().getOffsets());
*/

        Map<String, String> dataHash = new HashMap<>();
        hash.forEach((field, value) -> {
            dataHash.put(new String(field), new String(value));
        });
//        LOG.debug("Hash object: {}", dataHash);
        processHashData(key, dataHash);
    }

    private void processHashData(String key, Map<String, String> dataHash) {
        LOG.debug("Handling hash data for key: {}, record: {}", key, dataHash);
        boolean processed = false;
        DataRecord<Map<String, String>> record =
                new DataRecord<>(redisDataServer.getId(), LocalDateTime.now(), key, dataHash);
        for (DataPipe p : dataPipes) {
            Dataset sd = p.getFlow().getSourceDataset();
            boolean matched = sd.getType().equals("hash") && key.startsWith(sd.getDataset());
            if (!matched) continue;

            if (!liveTraffic) {
                if (sd.isTsCheck()) {
                    matched = matchTime(record, p.getFlow());
                    if (!matched) continue;
                }
                if (sd.isFingerprintCheck()) {
                    matched = matchFingerprint(record, p.getFlow());
                    if (!matched) continue;
                }
            }

            if (!sd.getDataFilter().isEmpty()) {
                matched = (Boolean) new ExpressionEvaluator().evaluateExpression(sd.getDataFilter(), dataHash);
                if (!matched) continue;
            }

            if (matched) p.addRecord(record);
            processed = true;
        }
        if (verifyMode) {
            if (processed) {
                verifyResponse.incrementMatched();
            } else {
                verifyResponse.incrementUnmatched();
            }
        }
    }

    private boolean matchFingerprint(DataRecord<Map<String, String>> record, Flow sub) {
        return true;
    }

    private boolean matchTime(DataRecord<Map<String, String>> record, Flow sub) {
        Dataset sd = sub.getSourceDataset();
        String updatedTime = record.getRecord().get(sd.getTsField());
        if (updatedTime == null) return true;
        LocalDateTime updatedTs = LocalDateTime.parse(updatedTime);
        LocalDateTime milestone = milestones.get(sub.getId());

        if (milestone == null) return true;
        //LOG.info("Updated time: {}, Milestone: {}, sd.getTsSeconds {}", updatedTs, milestone, sd.getTsSeconds());
        return updatedTs.isAfter(milestone.minusSeconds(sd.getTsSkipSeconds()));
    }

    private void handleStringEvent(KeyStringValueString event) {
        String key = new String(event.getKey());
        String value = new String(event.getValue());
//        LOG.debug("String event received. Key: {}, Value: {}, time : {}", key, value, event.getContext().getOffsets
//        ());
        // Add your string event processing logic here
    }

    private void handleSetEvent(KeyStringValueSet event) {
        String key = new String(event.getKey());
        Set<byte[]> set = event.getValue();
/*
        LOG.debug("Set event received. Key: {}, Member count: {}, Offsets : {}", key, set.size(),
                event.getContext().getOffsets());
*/

        // Log set members
        set.forEach(member -> {
            String memberStr = new String(member);
            //LOG.debug("Set member: {}", memberStr);
        });
        // Add your set event processing logic here
    }

    public void stop() {
        try {
            if (this.replicator != null) {
                replicator.close();
                replicator = null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error in closing replicator for redis : " + redisDataServer, e);
        }
    }
}
