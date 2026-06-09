package no.fdk.referencedata.digdir.evidencetype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class EvidenceTypeWriter {

    private final EvidenceTypeRepository evidenceTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public EvidenceTypeWriter(
            EvidenceTypeRepository evidenceTypeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.evidenceTypeRepository = evidenceTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<EvidenceType> items, RDFSource rdfSource) {
        evidenceTypeRepository.deleteAll();
        evidenceTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
