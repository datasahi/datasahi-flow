package datasahi.flow.commons.db;

import java.util.Objects;

public class SqlQuery {

    private final String sqlId;
    private final String sql;

    public SqlQuery(String sqlId, String query) {
        this.sqlId = sqlId;
        this.sql = query;
    }

    public String getSqlId() {
        return sqlId;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return "SqlQuery{" +
                "sqlId='" + sqlId + '\'' +
                ", sql='" + sql + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlQuery sqlQuery = (SqlQuery) o;
        return Objects.equals(sqlId, sqlQuery.sqlId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sqlId);
    }
}
