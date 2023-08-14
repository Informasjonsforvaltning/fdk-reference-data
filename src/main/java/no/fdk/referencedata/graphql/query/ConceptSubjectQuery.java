package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubject;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ConceptSubjectQuery implements GraphQLQueryResolver {

    @Autowired
    private ConceptSubjectRepository conceptSubjectRepository;

    public List<ConceptSubject> getConceptSubjects() {
        return StreamSupport.stream(conceptSubjectRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(ConceptSubject::getUri))
                .collect(Collectors.toList());
    }
}
