package datasahi.flow.commons.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.Map;

public class ServiceResponse<T> {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private boolean success = false;
    private long millis;
    private T data;
    private String errorId;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public ServiceResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public long getMillis() {
        return millis;
    }

    public ServiceResponse setMillis(long millis) {
        this.millis = millis;
        return this;
    }

    public T getData() {
        return data;
    }

    public ServiceResponse setData(T data) {
        this.data = data;
        return this;
    }

    public String getErrorId() {
        return errorId;
    }

    public ServiceResponse setErrorId(String errorId) {
        this.errorId = errorId;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ServiceResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public String toJsonString() {

        String dataJson = dataAsJsonText();

        StringBuilder sb = new StringBuilder(1024);
        sb.append('{');
        sb.append("\"data\":").append(dataJson);
        sb.append(",\"success\":").append(success);
        sb.append(",\"millis\":").append(millis);
        if (errorId != null) {
            sb.append(",\"errorId\":\"").append(errorId).append("\"");
        }
        if (message != null) {
            sb.append(",\"message\":\"").append(message).append("\"");
        }
        sb.append('}');
        return sb.toString();

    }

    private String dataAsJsonText() {
        String dataJson = "\"\"";
        if (data != null) {
            if (data instanceof JSONObject) {
                dataJson = ((JSONObject) data).toString();
            } else if (data instanceof JSONArray) {
                dataJson = ((JSONArray) data).toString();
            } else if (data instanceof Map) {
                dataJson = new JSONObject((Map) data).toString();
            } else if (data instanceof String) {
                dataJson = (String) data;
            } else {
                dataJson = gson.toJson(data);
            }
        }
        return dataJson;
    }

    @Override
    public String toString() {
        return "ServiceResponse{" +
                "success=" + success +
                ", millis=" + millis +
                ", data=" + data +
                ", errorId='" + errorId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
