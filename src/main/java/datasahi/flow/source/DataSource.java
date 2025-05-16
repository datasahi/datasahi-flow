package datasahi.flow.source;

import datasahi.flow.ds.DataServerType;
import datasahi.flow.health.VerifyRequest;
import datasahi.flow.health.VerifyResponse;
import datasahi.flow.sync.DataPipe;

import java.util.List;

public interface DataSource {

    String getId();
    DataServerType getType();
    void start();
    void stop();
    void addDataPipe(DataPipe dataPipe);
    VerifyResponse verify(VerifyRequest request);
    List<DataPipe> getDataPipes();
}
