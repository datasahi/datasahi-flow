package datasahi.flow.ds.grpcfs;

import datasahi.flow.ds.AbstractDataServer;
import datasahi.flow.ds.DataServerType;

public class GrpcFileServer extends AbstractDataServer {

    private String host;
    private int port;
    private String workFolder;

    public GrpcFileServer() {
        super(DataServerType.GRPCFS);
    }

    public String getHost() {
        return host;
    }

    public GrpcFileServer setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public GrpcFileServer setPort(int port) {
        this.port = port;
        return this;
    }

    public String getWorkFolder() {
        return workFolder;
    }

    public GrpcFileServer setWorkFolder(String workFolder) {
        this.workFolder = workFolder;
        return this;
    }

    @Override
    public String toString() {
        return "GrpcFileServer{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
