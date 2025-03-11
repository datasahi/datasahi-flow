package datasahi.flow.commons.db;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DatabaseConfig {

    private String url;
    private String user;
    private String password;
    private String driverClass;
    private List<String> sqlFiles = Collections.emptyList();
    private int minPoolSize;
    private int maxPoolSize;

    private Map<String, String> columnMappings = Collections.emptyMap();

    public String getUrl() {
        return url;
    }

    public DatabaseConfig setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUser() {
        return user;
    }

    public DatabaseConfig setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public DatabaseConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public DatabaseConfig setDriverClass(String driverClass) {
        this.driverClass = driverClass;
        return this;
    }

    public List<String> getSqlFiles() {
        return sqlFiles;
    }

    public DatabaseConfig setSqlFiles(List<String> sqlFiles) {
        this.sqlFiles = sqlFiles;
        return this;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public DatabaseConfig setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public Map<String, String> getColumnMappings() {
        return columnMappings;
    }

    public DatabaseConfig setColumnMappings(Map<String, String> columnMappings) {
        this.columnMappings = columnMappings;
        return this;
    }

    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "url='" + url + '\'' +
                ", user='" + user + '\'' +
                ", driverClass='" + driverClass + '\'' +
                ", sqlFiles=" + sqlFiles +
                ", minPoolSize=" + minPoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", columnMappings=" + columnMappings +
                '}';
    }
}
