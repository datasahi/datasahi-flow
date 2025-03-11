package datasahi.flow.api;

import datasahi.flow.commons.api.ServiceResponse;
import datasahi.flow.health.HealthCheckService;
import datasahi.flow.health.HealthSummary;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller("/health")
public class HealthController {

    private final HealthCheckService healthCheckService;

    public HealthController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @Produces(MediaType.TEXT_JSON)
    @Get("/summary")
    public String doHealthCheck() {
        long start = System.currentTimeMillis();
        HealthSummary healthSummary = healthCheckService.performHealthCheck();
        long millis = System.currentTimeMillis() - start;
        return new ServiceResponse<HealthSummary>().setSuccess(true).setData(healthSummary).setMillis(millis).toJsonString();
    }
}
