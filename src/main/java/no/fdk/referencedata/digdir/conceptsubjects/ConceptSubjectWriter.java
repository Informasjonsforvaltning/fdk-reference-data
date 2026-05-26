package no.fdk.referencedata.digdir.conceptsubjects;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ConceptSubjectWriter {

    private final ConceptSubjectRepository conceptSubjectRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ConceptSubjectWriter(ConceptSubjectRepository conceptSubjectRepository, RDFSourceRepository rdfSourceRepository) {
        this.conceptSubjectRepository = conceptSubjectRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<ConceptSubject> items, RDFSource rdfSource) {
        conceptSubjectRepository.deleteAll();
        conceptSubjectRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
