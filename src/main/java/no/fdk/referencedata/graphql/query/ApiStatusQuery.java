package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.apistatus.ApiStatus;
import no.fdk.referencedata.apistatus.ApiStatusService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ApiStatusQuery {

    private final ApiStatusService apiStatusService;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<ApiStatus> apiStatuses() {
        return support.allSortedByUri(apiStatusService.getAll(), ApiStatus::getUri);
    }

    @QueryMapping
    public ApiStatus apiStatusByCode(@Argument String code) {
        return support.byCode(apiStatusService::getByCode, code);
    }
}
