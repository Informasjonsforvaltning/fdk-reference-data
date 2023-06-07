package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatus;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ConceptStatusQuery implements GraphQLQueryResolver {

    @Autowired
    private ConceptStatusRepository conceptStatusRepository;

    public List<ConceptStatus> getConceptStatuses() {
        return StreamSupport.stream(conceptStatusRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(ConceptStatus::getUri))
                .collect(Collectors.toList());
    }

    public ConceptStatus getConceptStatusByCode(String code) {
        return conceptStatusRepository.findByCode(code).orElse(null);
    }
}
