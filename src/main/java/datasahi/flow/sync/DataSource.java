package datasahi.flow.sync;

import datasahi.flow.ds.DataServerType;
import datasahi.flow.health.VerifyRequest;
import datasahi.flow.health.VerifyResponse;

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
