package datasahi.flow.ds;

import datasahi.flow.commons.db.DatabaseConfig;

public class JdbcDataServer extends AbstractDataServer {

    private String url;
    private String user;
    private String password;
    private String driverClass;

    public JdbcDataServer() {
        super(DataServerType.JDBC);
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public JdbcDataServer setDriverClass(String driverClass) {
        this.driverClass = driverClass;
        return this;
    }

    public JdbcDataServer setUrl(String url) {
        this.url = url;
        return this;
    }

    public JdbcDataServer setUser(String user) {
        this.user = user;
        return this;
    }

    public JdbcDataServer setPassword(String password) {
        this.password = password;
        return this;
    }

    public DatabaseConfig getDatabaseConfig() {
        DatabaseConfig config = new DatabaseConfig();
        config.setDriverClass(getDriverClass()).setMaxPoolSize(1).setUrl(getUrl())
                .setUser(getUser()).setPassword(getPassword());
        return config;
    }
}
