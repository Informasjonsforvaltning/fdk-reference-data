package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubject;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class ConceptSubjectQuery {

    @Autowired
    private ConceptSubjectRepository conceptSubjectRepository;

    @QueryMapping
    public List<ConceptSubject> conceptSubjects() {
        return StreamSupport.stream(conceptSubjectRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(ConceptSubject::getUri))
                .collect(Collectors.toList());
    }
}
