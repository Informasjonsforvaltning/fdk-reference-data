package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.conceptstatus.ConceptStatus;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class ConceptStatusQuery {

    @Autowired
    private ConceptStatusRepository conceptStatusRepository;

    @QueryMapping
    public List<ConceptStatus> conceptStatuses() {
        return StreamSupport.stream(conceptStatusRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(ConceptStatus::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public ConceptStatus conceptStatusByCode(@Argument String code) {
        return conceptStatusRepository.findByCode(code).orElse(null);
    }
}
