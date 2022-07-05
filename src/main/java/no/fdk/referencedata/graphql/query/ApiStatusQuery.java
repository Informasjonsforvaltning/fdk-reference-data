package no.fdk.referencedata.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import no.fdk.referencedata.apistatus.ApiStatus;
import no.fdk.referencedata.apistatus.ApiStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApiStatusQuery implements GraphQLQueryResolver {

    private final ApiStatusService apiStatusService;

    @Autowired
    public ApiStatusQuery(ApiStatusService apiStatusService) {
        this.apiStatusService = apiStatusService;
    }

    public List<ApiStatus> getApiStatuses() {
        return apiStatusService.getAll().stream()
                .sorted(Comparator.comparing(ApiStatus::getUri))
                .collect(Collectors.toList());
    }

    public ApiStatus getApiStatusByCode(String code) {
        return apiStatusService.getByCode(code).orElse(null);
    }
}
