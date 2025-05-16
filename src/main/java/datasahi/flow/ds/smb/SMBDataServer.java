package datasahi.flow.ds.smb;

import datasahi.flow.ds.AbstractDataServer;
import datasahi.flow.ds.DataServerType;

public class SMBDataServer extends AbstractDataServer {

    private String host;
    private Integer port = 445;
    private String domain = "";
    private String username;
    private String password;
    private String shareName;
    private String rootPath = "/";
    private String url;

    public SMBDataServer() {
        super(DataServerType.SMBFS);
    }

    public SMBDataServer setHost(String host) {
        this.host = host;
        return this;
    }

    public SMBDataServer setPort(Integer port) {
        this.port = port;
        return this;
    }

    public SMBDataServer setUsername(String username) {
        this.username = username;
        return this;
    }

    public SMBDataServer setPassword(String password) {
        this.password = password;
        return this;
    }

    public SMBDataServer setUrl(String url) {
        this.url = url;
        return this;
    }

    // Getters for all properties
    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getDomain() {
        return domain;
    }

    public SMBDataServer setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getShareName() {
        return shareName;
    }

    public SMBDataServer setShareName(String shareName) {
        this.shareName = shareName;
        return this;
    }

    public String getRootPath() {
        return rootPath;
    }

    public SMBDataServer setRootPath(String rootPath) {
        this.rootPath = rootPath;
        return this;
    }
}