package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.lovdata.LegalResource;
import no.fdk.referencedata.lovdata.LegalResourceRepository;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LegalResourceQuery {

    private final LegalResourceRepository legalResourceRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<LegalResource> legalResources() {
        return support.allSortedByUri(legalResourceRepository, LegalResource::getUri);
    }
}
