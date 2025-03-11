package datasahi.flow.ds;

public class RedisDataServer extends AbstractDataServer {

    private String url;
    private String host;
    private int port;

    public RedisDataServer() {
        super(DataServerType.REDIS);
    }

    public RedisDataServer setHost(String host) {
        this.host = host;
        return this;
    }

    public RedisDataServer setPort(int port) {
        this.port = port;
        return this;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUrl() {
        return url;
    }

    public RedisDataServer setUrl(String url) {
        this.url = url;
        return this;
    }
}
