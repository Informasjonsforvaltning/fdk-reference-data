package no.fdk.referencedata.eu.conceptstatus;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ConceptStatusWriter {

    private final ConceptStatusRepository conceptStatusRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ConceptStatusWriter(
            ConceptStatusRepository conceptStatusRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.conceptStatusRepository = conceptStatusRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<ConceptStatus> items, RDFSource rdfSource) {
        conceptStatusRepository.deleteAll();
        conceptStatusRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
