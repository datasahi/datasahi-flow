package datasahi.flow.ds.localfs;

import datasahi.flow.ds.AbstractDataServer;
import datasahi.flow.ds.DataServerType;

public class LocalFileServer extends AbstractDataServer {

    private String dataFolder;
    private String workFolder;
    private String archiveFolder;
    private String errorFolder;

    public LocalFileServer() {
        super(DataServerType.LOCALFS);
    }

    public String getDataFolder() {
        return dataFolder;
    }

    public LocalFileServer setDataFolder(String dataFolder) {
        this.dataFolder = dataFolder;
        return this;
    }

    public String getWorkFolder() {
        return workFolder;
    }

    public LocalFileServer setWorkFolder(String workFolder) {
        this.workFolder = workFolder;
        return this;
    }

    public String getArchiveFolder() {
        return archiveFolder;
    }

    public LocalFileServer setArchiveFolder(String archiveFolder) {
        this.archiveFolder = archiveFolder;
        return this;
    }

    public String getErrorFolder() {
        return errorFolder;
    }

    public LocalFileServer setErrorFolder(String errorFolder) {
        this.errorFolder = errorFolder;
        return this;
    }

    @Override
    public String toString() {
        return "LocalFileServer{" +
                "dataFolder='" + dataFolder + '\'' +
                ", workFolder='" + workFolder + '\'' +
                ", archiveFolder='" + archiveFolder + '\'' +
                ", errorFolder='" + errorFolder + '\'' +
                '}';
    }
}
