package datasahi.flow.sink;

import datasahi.flow.commons.db.DatabaseService;
import datasahi.flow.ds.DataServerType;
import datasahi.flow.ds.JdbcDataServer;
import datasahi.flow.ms.MilestoneService;
import datasahi.flow.sync.DataHolder;
import datasahi.flow.sync.DataRecord;
import datasahi.flow.sync.Flow;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.*;

public class JdbcSink implements DataSink {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(JdbcSink.class);

    private final Map<String, Flow> flows = new HashMap<>();
    private final JdbcDataServer jdbcDataServer;
    private final MilestoneService milestoneService;

    private DatabaseService dbService;

    public JdbcSink(JdbcDataServer sink, MilestoneService milestoneService) {
        this.jdbcDataServer = sink;
        this.milestoneService = milestoneService;
    }

    @Override
    public String getId() {
        return jdbcDataServer.getId();
    }

    @Override
    public DataServerType getType() {
        return jdbcDataServer.getType();
    }

    public void process(DataHolder dataHolder) {
/*
        LOG.debug("Processing data for flow: {}, record count : {}", dataHolder.getFlowId(), dataHolder
        .fetch().size());
*/
        dataHolder.fetch().forEach(r -> {
            Map<String, Object> data = (Map<String, Object>) ((DataRecord) r).getRecord();
            String sqlId = dataHolder.getFlowId() + ":update";
            int updated = dbService.updateWithMap(sqlId, data);
            if (updated == 0) {
                sqlId = dataHolder.getFlowId() + ":create";
                dbService.updateWithMap(sqlId, data);
            }
        });

        updateMilestone(dataHolder);
    }

    public void processBatch(DataHolder dataHolder) {
        LOG.info("Processing data for flow: {}, record count : {}", dataHolder.getFlowId(), dataHolder
                .fetch().size());

        List<Map<String, Object>> paramsList = new ArrayList<>();
        dataHolder.fetch().forEach(r -> {
            Map<String, Object> data = (Map<String, Object>) ((DataRecord) r).getRecord();
            paramsList.add(data);
        });

        int[] allUpdated = dbService.batchUpdateWithMap(dataHolder.getFlowId() + ":update", paramsList);
        LOG.info("Syncing data by update for flow: {}, input count: {}, update count: {}",
                dataHolder.getFlowId(),
                paramsList.size(), Arrays.stream(allUpdated).sum());

        Flow sub = flows.get(dataHolder.getFlowId());
        String idField = sub.getSourceDataset().getIdField();
        List<Map<String, Object>> createList = new ArrayList<>();
        List<Map<String, Object>> updateList = new ArrayList<>();
        Set<Object> createIds = new HashSet<>();
        for (int i = 0; i < allUpdated.length; i++) {
            if (allUpdated[i] == 0) {
                Object id = paramsList.get(i).get(idField);
                if (id == null) continue;
                if (createIds.contains(id)) {
                    updateList.add(paramsList.get(i));
                } else {
                    createList.add(paramsList.get(i));
                    createIds.add(id);
                }
            }
        }
        LOG.info("Syncing data by create+update for flow: {}, create count: {}, update count: {}",
                dataHolder.getFlowId(),
                createList.size(), updateList.size());
        if (!createList.isEmpty()) {
            int[] created = dbService.batchUpdateWithMap(dataHolder.getFlowId() + ":create", createList);
        }
        if (!updateList.isEmpty()) {
            int[] updated = dbService.batchUpdateWithMap(dataHolder.getFlowId() + ":update", updateList);
        }

        updateMilestone(dataHolder);
    }

    private void updateMilestone(DataHolder dataHolder) {
        Flow sub = flows.get(dataHolder.getFlowId());
        LOG.info("Checking for milestone update for flow {}, with tsFilter {}", sub.getId(),
                sub.getSourceDataset().isTsCheck());
        if (!sub.getSourceDataset().isTsCheck()) return;

        Map<String, Object> record = (Map<String, Object>) ((DataRecord) dataHolder.fetch().getLast()).getRecord();
        LOG.info("Updating milestone for record {} from field {}", record, sub.getSourceDataset().getTsField());
        if (record.containsKey(sub.getSourceDataset().getTsField())) {
            String updatedTime = (String) record.get(sub.getSourceDataset().getTsField());
            LocalDateTime milestone = LocalDateTime.parse(updatedTime);
            milestoneService.store(sub.getId(), milestone);
        }
    }

    @Override
    public void start() {
        this.dbService = new DatabaseService(jdbcDataServer.getDatabaseConfig());
        flows.values().forEach(s -> {
            addSql(s.getSinkDataset().getCrud().getCreate(), s.getId() + ":create");
            addSql(s.getSinkDataset().getCrud().getRead(), s.getId() + ":read");
            addSql(s.getSinkDataset().getCrud().getUpdate(), s.getId() + ":update");
            addSql(s.getSinkDataset().getCrud().getDelete(), s.getId() + ":delete");
        });
    }

    private void addSql(String sql, String sqlId) {
        if (StringUtils.isEmpty(sql)) return;
        dbService.getSqlRepository().addSql(sqlId, sql);
    }

    @Override
    public void stop() {

    }

    @Override
    public void addFlow(Flow flow) {
        flows.put(flow.getId(), flow);
    }

    @Override
    public List<Flow> getFlows() {
        return new ArrayList<>(flows.values());
    }
}
