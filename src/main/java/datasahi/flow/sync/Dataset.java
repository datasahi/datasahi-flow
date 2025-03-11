package datasahi.flow.sync;

public class Dataset {

    private String server;
    private String type;
    private String dataset;
    private String dataFilter;
    private CrudDefinition crud;
    private int queueSize;
    private boolean fingerprintCheck;
    private boolean tsCheck;
    private int tsSkipSeconds;
    private String tsField;
    private String idField;

    public String getServer() {
        return server;
    }

    public Dataset setServer(String server) {
        this.server = server;
        return this;
    }

    public String getType() {
        return type;
    }

    public Dataset setType(String type) {
        this.type = type;
        return this;
    }

    public String getDataset() {
        return dataset;
    }

    public Dataset setDataset(String dataset) {
        this.dataset = dataset;
        return this;
    }

    public String getDataFilter() {
        return dataFilter;
    }

    public Dataset setDataFilter(String dataFilter) {
        this.dataFilter = dataFilter;
        return this;
    }

    public CrudDefinition getCrud() {
        return crud;
    }

    public Dataset setCrud(CrudDefinition crud) {
        this.crud = crud;
        return this;
    }

    public boolean isTsCheck() {
        return tsCheck;
    }

    public Dataset setTsCheck(boolean tsCheck) {
        this.tsCheck = tsCheck;
        return this;
    }

    public String getTsField() {
        return tsField;
    }

    public Dataset setTsField(String tsField) {
        this.tsField = tsField;
        return this;
    }

    public int getTsSkipSeconds() {
        return tsSkipSeconds;
    }

    public Dataset setTsSkipSeconds(int tsSkipSeconds) {
        this.tsSkipSeconds = tsSkipSeconds;
        return this;
    }

    public boolean isFingerprintCheck() {
        return fingerprintCheck;
    }

    public Dataset setFingerprintCheck(boolean fingerprintCheck) {
        this.fingerprintCheck = fingerprintCheck;
        return this;
    }

    public String getIdField() {
        return idField;
    }

    public Dataset setIdField(String idField) {
        this.idField = idField;
        return this;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public Dataset setQueueSize(int queueSize) {
        this.queueSize = queueSize;
        return this;
    }

    @Override
    public String toString() {
        return "Dataset{" +
                "server='" + server + '\'' +
                ", type='" + type + '\'' +
                ", dataset='" + dataset + '\'' +
                ", dataFilter='" + dataFilter + '\'' +
                ", crud=" + crud +
                ", queueSize=" + queueSize +
                ", fingerprintCheck=" + fingerprintCheck +
                ", tsCheck=" + tsCheck +
                ", tsSkipSeconds=" + tsSkipSeconds +
                ", tsField='" + tsField + '\'' +
                ", idField='" + idField + '\'' +
                '}';
    }
}
