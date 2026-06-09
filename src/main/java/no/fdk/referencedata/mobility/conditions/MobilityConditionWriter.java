package no.fdk.referencedata.mobility.conditions;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class MobilityConditionWriter {

    private final MobilityConditionRepository mobilityConditionRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MobilityConditionWriter(
            MobilityConditionRepository mobilityConditionRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.mobilityConditionRepository = mobilityConditionRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<MobilityCondition> items, RDFSource rdfSource) {
        mobilityConditionRepository.deleteAll();
        mobilityConditionRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
