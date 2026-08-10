package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceType;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceTypeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LegalResourceTypeQuery {

    private final LegalResourceTypeRepository legalResourceTypeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<LegalResourceType> legalResourceTypes() {
        return support.allSortedByUri(legalResourceTypeRepository, LegalResourceType::getUri);
    }

    @QueryMapping
    public LegalResourceType legalResourceTypeByCode(@Argument String code) {
        return support.byCode(legalResourceTypeRepository::findByCode, code);
    }
}
