package no.fdk.referencedata.los;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class LosWriter {

    private final LosRepository losRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public LosWriter(LosRepository losRepository, RDFSourceRepository rdfSourceRepository) {
        this.losRepository = losRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<LosNode> losNodes, RDFSource rdfSource) {
        losRepository.deleteAll();
        losRepository.saveAll(losNodes);
        rdfSourceRepository.save(rdfSource);
    }
}
