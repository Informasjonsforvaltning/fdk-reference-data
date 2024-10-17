package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.apistatus.ApiStatus;
import no.fdk.referencedata.apistatus.ApiStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ApiStatusQuery {

    private final ApiStatusService apiStatusService;

    @Autowired
    public ApiStatusQuery(ApiStatusService apiStatusService) {
        this.apiStatusService = apiStatusService;
    }

    @QueryMapping
    public List<ApiStatus> apiStatuses() {
        return apiStatusService.getAll().stream()
                .sorted(Comparator.comparing(ApiStatus::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public ApiStatus apiStatusByCode(@Argument String code) {
        return apiStatusService.getByCode(code).orElse(null);
    }
}
