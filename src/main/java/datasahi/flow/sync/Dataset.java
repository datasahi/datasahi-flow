package datasahi.flow.sync;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.json.JSONObject;

import java.io.IOException;

public class Dataset {

    private String server;
    private String type;
    private String dataset;
    private String dataFilter;
    private CrudDefinition crud;
    private int queueSize = 10;
    private boolean fingerprintCheck;
    private boolean tsCheck;
    private int tsSkipSeconds;
    private String tsField;
    private String idField;
    private JSONObject configJson;

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

    public JSONObject getConfigJson() {
        return configJson;
    }

    public Dataset setConfigJson(JSONObject configJson) {
        this.configJson = configJson;
        return this;
    }

    // Custom Gson TypeAdapter for JSONObject
    public static class JSONObjectTypeAdapter extends TypeAdapter<JSONObject> {

        @Override
        public void write(JsonWriter out, JSONObject value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.jsonValue(value.toString());
        }

        @Override
        public JSONObject read(JsonReader in) throws IOException {
            String jsonString = in.nextString();
            return new JSONObject(jsonString);
        }
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
                ", config=" + configJson +
                '}';
    }
}
