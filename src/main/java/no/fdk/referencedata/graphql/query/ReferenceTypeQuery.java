package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.referencetypes.ReferenceType;
import no.fdk.referencedata.referencetypes.ReferenceTypeService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReferenceTypeQuery {

    private final ReferenceTypeService referenceTypeService;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<ReferenceType> referenceTypes() {
        return support.allSorted(referenceTypeService.getAll(), Comparator.comparing(ReferenceType::getCode));
    }

    @QueryMapping
    public ReferenceType referenceTypeByCode(@Argument String code) {
        return support.byCode(referenceTypeService::getByCode, code);
    }
}
