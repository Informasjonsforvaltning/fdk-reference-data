package no.fdk.referencedata.eu.distributionstatus;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DistributionStatusWriter {

    private final DistributionStatusRepository distributionStatusRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public DistributionStatusWriter(
            DistributionStatusRepository distributionStatusRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.distributionStatusRepository = distributionStatusRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<DistributionStatus> items, RDFSource rdfSource) {
        distributionStatusRepository.deleteAll();
        distributionStatusRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
