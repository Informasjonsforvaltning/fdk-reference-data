package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubject;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ConceptSubjectQuery {

    private final ConceptSubjectRepository conceptSubjectRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<ConceptSubject> conceptSubjects() {
        return support.allSortedByUri(conceptSubjectRepository, ConceptSubject::getUri);
    }

    @QueryMapping
    public ConceptSubject conceptSubjectByCode(@Argument String code) {
        return support.byCode(conceptSubjectRepository::findByCode, code);
    }

}
