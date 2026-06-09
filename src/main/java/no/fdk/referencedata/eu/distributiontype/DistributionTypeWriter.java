package no.fdk.referencedata.eu.distributiontype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DistributionTypeWriter {

    private final DistributionTypeRepository distributionTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public DistributionTypeWriter(
            DistributionTypeRepository distributionTypeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.distributionTypeRepository = distributionTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<DistributionType> items, RDFSource rdfSource) {
        distributionTypeRepository.deleteAll();
        distributionTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
