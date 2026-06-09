package no.fdk.referencedata.digdir.legalresourcetype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class LegalResourceTypeWriter {

    private final LegalResourceTypeRepository legalResourceTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public LegalResourceTypeWriter(
            LegalResourceTypeRepository legalResourceTypeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.legalResourceTypeRepository = legalResourceTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<LegalResourceType> items, RDFSource rdfSource) {
        legalResourceTypeRepository.deleteAll();
        legalResourceTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
