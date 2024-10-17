package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.apispecification.ApiSpecification;
import no.fdk.referencedata.apispecification.ApiSpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ApiSpecificationQuery {

    private final ApiSpecificationService apiSpecificationService;

    @Autowired
    public ApiSpecificationQuery(ApiSpecificationService apiSpecificationService) {
        this.apiSpecificationService = apiSpecificationService;
    }

    @QueryMapping
    public List<ApiSpecification> apiSpecifications() {
        return apiSpecificationService.getAll().stream()
                .sorted(Comparator.comparing(ApiSpecification::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public ApiSpecification apiSpecificationByCode(@Argument String code) {
        return apiSpecificationService.getByCode(code).orElse(null);
    }
}
