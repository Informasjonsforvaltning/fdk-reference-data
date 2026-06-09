package no.fdk.referencedata.eu.accessright;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AccessRightWriter {

    private final AccessRightRepository accessRightRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public AccessRightWriter(
            AccessRightRepository accessRightRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.accessRightRepository = accessRightRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<AccessRight> items, RDFSource rdfSource) {
        accessRightRepository.deleteAll();
        accessRightRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
