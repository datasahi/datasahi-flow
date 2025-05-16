package datasahi.flow.sync;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

public class Flow {

    private final JSONObject definition;
    private final String id;
    private final Dataset sourceDataset;
    private final Dataset sinkDataset;
    private final BatchInfo batchInfo;

    public Flow(JSONObject definition) {
        this.definition = definition;
        this.id = definition.getString("id");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JSONObject.class, new Dataset.JSONObjectTypeAdapter())
                .create();
        this.sourceDataset = gson.fromJson(definition.getJSONObject("source").toString(), Dataset.class);
        if (definition.getJSONObject("source").has("config")) {
            this.sourceDataset.setConfigJson(definition.getJSONObject("source").getJSONObject("config"));
        };
        this.sinkDataset = gson.fromJson(definition.getJSONObject("sink").toString(), Dataset.class);
        if (definition.getJSONObject("sink").has("config")) {
            this.sinkDataset.setConfigJson(definition.getJSONObject("sink").getJSONObject("config"));
        };
        this.batchInfo = (definition.has("batch")) ?
                gson.fromJson(definition.getJSONObject("batch").toString(), BatchInfo.class) : new BatchInfo();
    }

    public String getId() {
        return id;
    }

    public BatchInfo getBatchInfo() {
        return batchInfo;
    }

    public String getSourceId() {
        return sourceDataset.getServer();
    }

    public String getSinkId() {
        return sinkDataset.getServer();
    }

    public Dataset getSourceDataset() {
        return sourceDataset;
    }

    public Dataset getSinkDataset() {
        return sinkDataset;
    }

    @Override
    public String toString() {
        return "Flow{" +
                "definition=" + definition +
                ", id='" + id + '\'' +
                ", sourceDataset=" + sourceDataset +
                ", sinkDataset=" + sinkDataset +
                ", batchInfo=" + batchInfo +
                '}';
    }
}
