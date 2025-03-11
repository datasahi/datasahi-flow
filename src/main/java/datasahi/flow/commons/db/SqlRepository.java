package datasahi.flow.commons.db;

import datasahi.flow.commons.file.FileUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlRepository {

    private List<String> sqlFiles = Collections.emptyList();
    private Map<String, SqlQuery> queryMap;

    public SqlRepository(List<String> sqlFiles) {
        this.sqlFiles = sqlFiles;
        this.init();
    }

    public void init() {
        this.queryMap = new HashMap<>();
        if (sqlFiles == null || sqlFiles.isEmpty()) return;
        for (String sqlSourcePath : sqlFiles) {
            loadSqls(sqlSourcePath);
        }
    }

    public SqlQuery getSqlQuery(String sqlId) {
        return queryMap.get(sqlId);
    }

    public String getSql(String sqlId) {
        SqlQuery sqlQuery = queryMap.get(sqlId);
        if (sqlQuery == null) {
            throw new IllegalArgumentException(sqlId + " is not configured in sql repo");
        }
        return sqlQuery.getSql();
    }

    public void addSql(String sqlId, String sql) {
        queryMap.put(sqlId, new SqlQuery(sqlId, sql));
    }

    private void loadSqls(String sqlSourcePath) {
        JSONObject sqlsJson = new FileUtil().readJsonFile(sqlSourcePath);
        if (!sqlsJson.has("sqls")) return;

        JSONArray sqls = sqlsJson.getJSONArray("sqls");
        for (int i = 0; i < sqls.length(); i++) {
            JSONObject sqlJson = sqls.getJSONObject(i);
            String sqlId = sqlJson.getString("sqlId");
            queryMap.put(sqlId, new SqlQuery(sqlId, sqlJson.getString("sql")));
        }
    }
}