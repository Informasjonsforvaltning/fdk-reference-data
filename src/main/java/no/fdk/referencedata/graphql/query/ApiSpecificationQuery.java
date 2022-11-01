package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.apispecification.ApiSpecification;
import no.fdk.referencedata.apispecification.ApiSpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApiSpecificationQuery implements GraphQLQueryResolver {

    private final ApiSpecificationService apiSpecificationService;

    @Autowired
    public ApiSpecificationQuery(ApiSpecificationService apiSpecificationService) {
        this.apiSpecificationService = apiSpecificationService;
    }

    public List<ApiSpecification> getApiSpecifications() {
        return apiSpecificationService.getAll().stream()
                .sorted(Comparator.comparing(ApiSpecification::getUri))
                .collect(Collectors.toList());
    }

    public ApiSpecification getApiSpecificationByCode(String code) {
        return apiSpecificationService.getByCode(code).orElse(null);
    }
}
