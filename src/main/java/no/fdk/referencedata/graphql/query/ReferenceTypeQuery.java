package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.referencetypes.ReferenceType;
import no.fdk.referencedata.referencetypes.ReferenceTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReferenceTypeQuery implements GraphQLQueryResolver {

    private final ReferenceTypeService referenceTypeService;

    @Autowired
    public ReferenceTypeQuery(ReferenceTypeService referenceTypeService) {
        this.referenceTypeService = referenceTypeService;
    }

    public List<ReferenceType> getReferenceTypes() {
        return referenceTypeService.getAll().stream()
                .sorted(Comparator.comparing(ReferenceType::getCode))
                .collect(Collectors.toList());
    }

    public ReferenceType getReferenceTypeByCode(String code) {
        return referenceTypeService.getByCode(code).orElse(null);
    }
}
