package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatus;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ConceptStatusQuery {

    private final ConceptStatusRepository conceptStatusRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<ConceptStatus> conceptStatuses() {
        return support.allSortedByUri(conceptStatusRepository, ConceptStatus::getUri);
    }

    @QueryMapping
    public ConceptStatus conceptStatusByCode(@Argument String code) {
        return support.byCode(conceptStatusRepository::findByCode, code);
    }
}
