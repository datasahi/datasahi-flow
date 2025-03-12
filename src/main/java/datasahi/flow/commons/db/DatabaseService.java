package datasahi.flow.commons.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Column;
import org.sql2o.data.Row;
import org.sql2o.data.Table;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseService {

    private SqlRepository sqlRepository;
    private Sql2o sql2o;
    private DatabaseConfig config;
    private boolean returnGeneratedKeys = false;
    private final HikariDataSource dataSource;

    public DatabaseService(DatabaseConfig config) {
        this.config = config;
        this.dataSource = new HikariDataSource(createHikariConfig());
        this.sql2o = new Sql2o(dataSource);
        this.sqlRepository = new SqlRepository(config.getSqlFiles());
        this.sql2o.setDefaultColumnMappings(config.getColumnMappings());
    }

    public void close() {
        if (this.dataSource == null || this.dataSource.isClosed()) return;
        this.dataSource.close();
    }

    private HikariConfig createHikariConfig() {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(config.getUrl());
        hc.setUsername(config.getUser());
        hc.setPassword(config.getPassword());
        hc.setMaximumPoolSize(config.getMaxPoolSize());
        if (config.getDriverClass().contains("duckdb")) {
            returnGeneratedKeys = false;
            hc.addDataSourceProperty("duckdb.read_only", "false");
        } else if (config.getDriverClass().contains("sqlserver")) {
            returnGeneratedKeys = false;
        } else {
            returnGeneratedKeys = true;
            hc.addDataSourceProperty("cachePrepStmts", "true");
            hc.addDataSourceProperty("prepStmtCacheSize", "250");
            hc.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        }
        //System.out.println("config.getDriverClass() :: " + config.getDriverClass() + ", returnGeneratedKeys :: " +
        // returnGeneratedKeys);
        return hc;
    }

    public SqlRepository getSqlRepository() {
        return sqlRepository;
    }

    public DatabaseConfig getConfig() {
        return config;
    }

    public String selectAsText(String sqlId, Map<String, Object> parameters, OutputFormat outputFormat) {
        return selectAsText(sqlId, parameters, outputFormat, Collections.emptySet());
    }

    public String selectAsText(String sqlId, Map<String, Object> parameters, OutputFormat outputFormat,
                               Set<String> jsonColumns) {

        try (Connection con = sql2o.open()) {
            Table records = getQuery(parameters, sqlId, con).executeAndFetchTable();

            if (outputFormat == OutputFormat.CSV) {
                return prepareCsv(records);
            } else {
                return prepareJson(records, jsonColumns);
            }
        }
    }

    private String prepareJson(Table records, Set<String> jsonColumns) {
        StringBuilder sb = new StringBuilder();
        List<String> columnsText = records.columns().stream().map(Column::getName).collect(Collectors.toList());
//        records.columns().stream().forEach(c -> System.out.println(c.getName() + " :: " + c.getType()));
        List<Boolean> numericTypes = records.columns().stream()
                .map(c -> (c.getType().startsWith("int") || c.getType().contains("serial") ||
                        c.getType().equals("numeric")))
                .collect(Collectors.toList());

        int columnCount = records.columns().size();
        sb.append('[');
        boolean firstRecord = true;
        for (Row row : records.rows()) {
            if (!firstRecord) {
                sb.append(',');
            }
            sb.append('{');
            boolean firstColumn = true;
            for (int i = 0; i < columnCount; i++) {
                if (!firstColumn) {
                    sb.append(',');
                }
                String columnName = columnsText.get(i);
                sb.append('"').append(columnName).append('"').append(':');
                if (!numericTypes.get(i) && !jsonColumns.contains(columnName)) {
                    sb.append('"');
                }
                sb.append(row.getObject(i));
                if (!numericTypes.get(i) && !jsonColumns.contains(columnName)) {
                    sb.append('"');
                }
                firstColumn = false;
            }
            sb.append('}');
            firstRecord = false;
        }
        sb.append(']');
        return sb.toString();
    }

    private String prepareCsv(Table records) {
        StringBuilder sb = new StringBuilder();
        List<String> columnsText = records.columns().stream().map(Column::getName).collect(Collectors.toList());
        sb.append(StringUtils.join(columnsText, '|')).append('\n');
        int columnCount = records.columns().size();
        for (Row row : records.rows()) {
            Object[] values = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                values[i] = row.getObject(i);
            }
            sb.append(StringUtils.join(values, '|')).append('\n');
        }
        return sb.toString();
    }

    public <T> List<T> select(String sqlId, Map<String, Object> parameters, Class<T> recordType) {

        try (Connection con = sql2o.open()) {
            return getQuery(parameters, sqlId, con).executeAndFetch(recordType);
        }
    }

    public <T> T selectOne(String sqlId, Map<String, Object> parameters, Class<T> recordType) {

        try (Connection con = sql2o.open()) {
            return getQuery(parameters, sqlId, con).executeAndFetchFirst(recordType);
        }
    }

    public <T> T selectScalar(String sqlId, Map<String, Object> parameters, Class<T> recordType) {

        try (Connection con = sql2o.open()) {
            return getQuery(parameters, sqlId, con).executeScalar(recordType);
        }
    }

    public <T> List<T> selectScalarList(String sqlId, Map<String, Object> parameters, Class<T> recordType) {

        try (Connection con = sql2o.open()) {
            return getQuery(parameters, sqlId, con).executeScalarList(recordType);
        }
    }

    public int updateWithMap(String sqlId, Map<String, Object> parameters) {

//        System.out.println("sqlid :: " + sqlId + ". Params :: " + parameters);
        try (Connection con = sql2o.open()) {
            getQuery(parameters, sqlId, con).executeUpdate();
            return con.getResult();
        }
    }

    public int updateWithObject(String sqlId, Object model) {

//        System.out.println("sqlid :: " + sqlId + ". Params :: " + parameters);
        try (Connection con = sql2o.open()) {
            String sql = sqlRepository.getSql(sqlId);
            con.createQuery(sql, returnGeneratedKeys).bind(model).executeUpdate();
            return con.getResult();
        }
    }

    public int executeUpdateSql(String sql) {

//        System.out.println("sqlid :: " + sqlId + ". Params :: " + parameters);
        try (Connection con = sql2o.open()) {
            con.createQuery(sql, returnGeneratedKeys).executeUpdate();
            return con.getResult();
        }
    }

    public Object executeSelectSql(String sql) {

        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).executeScalar();
        }
    }

    public int[] batchUpdateWithMap(String sqlId, List<Map<String, Object>> parametersList) {

        String sql = sqlRepository.getSql(sqlId);

        try (Connection con = sql2o.beginTransaction()) {
            Query query = con.createQuery(sql, returnGeneratedKeys);
            int size = parametersList.size();
            for (int i = 0; i < size; i++) {
                Map<String, Object> parameters = parametersList.get(i);
                for (String key : parameters.keySet()) {
                    if (key.equals("_class")) continue;
                    try {
                        query.addParameter(key, parameters.get(key));
                    } catch (Exception e) {
                        // Nothing to do, this can be ignored
                    }
                }
                query.addToBatch();
            }
            query.executeBatch(); // executes entire batch
            con.commit();         // remember to call commit(), else sql2o will automatically rollback.
            return con.getBatchResult();
        }
    }

    private Query getQuery(Map<String, Object> parameters, String sqlId, Connection con) {

//        System.out.println("sqlId ::" + sqlId);
        String sql = sqlRepository.getSql(sqlId);
        Query query = con.createQuery(sql, returnGeneratedKeys);
        for (String key : parameters.keySet()) {
            try {
//                System.out.println(key + "::" + parameters.get(key));
                query.addParameter(key, parameters.get(key));
            } catch (Exception e) {
                // Nothing to do, this can be ignored
                //System.out.println("Error :: " + e.getMessage());
            }
        }
//        System.out.println("Query :: " + query + ". Parameters :: " + parameters);
        return query;
    }
}
