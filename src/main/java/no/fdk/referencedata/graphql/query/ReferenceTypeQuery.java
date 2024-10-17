package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.referencetypes.ReferenceType;
import no.fdk.referencedata.referencetypes.ReferenceTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ReferenceTypeQuery {

    private final ReferenceTypeService referenceTypeService;

    @Autowired
    public ReferenceTypeQuery(ReferenceTypeService referenceTypeService) {
        this.referenceTypeService = referenceTypeService;
    }

    @QueryMapping
    public List<ReferenceType> referenceTypes() {
        return referenceTypeService.getAll().stream()
                .sorted(Comparator.comparing(ReferenceType::getCode))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public ReferenceType referenceTypeByCode(@Argument String code) {
        return referenceTypeService.getByCode(code).orElse(null);
    }
}
