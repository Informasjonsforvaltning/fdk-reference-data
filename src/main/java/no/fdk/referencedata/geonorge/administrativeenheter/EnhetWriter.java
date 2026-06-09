package no.fdk.referencedata.geonorge.administrativeenheter;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class EnhetWriter {

    private final EnhetRepository enhetRepository;
    private final EnhetVariantRepository enhetVariantRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public EnhetWriter(
            EnhetRepository enhetRepository,
            EnhetVariantRepository enhetVariantRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.enhetRepository = enhetRepository;
        this.enhetVariantRepository = enhetVariantRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(
            List<Enhet> enheter,
            List<EnhetVariant> docVariants,
            List<EnhetVariant> idVariants,
            RDFSource rdfSource) {
        enhetRepository.deleteAll();
        enhetVariantRepository.deleteAll();
        enhetRepository.saveAll(enheter);
        enhetVariantRepository.saveAll(docVariants);
        enhetVariantRepository.saveAll(idVariants);
        rdfSourceRepository.save(rdfSource);
    }
}
