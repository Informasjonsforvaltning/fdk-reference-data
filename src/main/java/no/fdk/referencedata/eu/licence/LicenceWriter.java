package no.fdk.referencedata.eu.licence;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class LicenceWriter {

    private final LicenceRepository licenceRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public LicenceWriter(
            LicenceRepository licenceRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.licenceRepository = licenceRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<Licence> items, RDFSource rdfSource) {
        licenceRepository.deleteAll();
        licenceRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
