package datasahi.flow.api;

import datasahi.flow.commons.api.ServiceResponse;
import datasahi.flow.health.HealthSummary;
import datasahi.flow.health.VerifyRequest;
import datasahi.flow.health.VerifyResponse;
import datasahi.flow.sync.DataSyncService;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller("/sync")
public class SyncController {

    private final DataSyncService dataSyncService;

    public SyncController(DataSyncService dataSyncService) {
        this.dataSyncService = dataSyncService;
    }

    @Produces(MediaType.TEXT_JSON)
    @Get("/start")
    public String startSync() {
        String message = dataSyncService.start();
        return new ServiceResponse<HealthSummary>().setSuccess(true).setMessage(message).toJsonString();
    }

    @Produces(MediaType.TEXT_JSON)
    @Get("/stop")
    public String stopSync() {
        String message = dataSyncService.stop();
        return new ServiceResponse<HealthSummary>().setSuccess(true).setMessage(message).toJsonString();
    }

    @Produces(MediaType.TEXT_JSON)
    @Get("/verify/{flowId}/{maxRecordCount}/{maxSeconds}")
    public String startSync(@Parameter("flowId") String flowId, @Parameter("maxRecordCount") int maxRecordCount,
                            @Parameter("maxSeconds") int maxSeconds) {
        VerifyRequest request = new VerifyRequest(flowId, maxRecordCount, maxSeconds);
        VerifyResponse response = dataSyncService.verify(request);
        return new ServiceResponse<HealthSummary>().setSuccess(true).setData(response).toJsonString();
    }
}
