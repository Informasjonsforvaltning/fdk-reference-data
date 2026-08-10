package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.apispecification.ApiSpecification;
import no.fdk.referencedata.apispecification.ApiSpecificationService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ApiSpecificationQuery {

    private final ApiSpecificationService apiSpecificationService;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<ApiSpecification> apiSpecifications() {
        return support.allSortedByUri(apiSpecificationService.getAll(), ApiSpecification::getUri);
    }

    @QueryMapping
    public ApiSpecification apiSpecificationByCode(@Argument String code) {
        return support.byCode(apiSpecificationService::getByCode, code);
    }
}
