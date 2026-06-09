package no.fdk.referencedata.eu.plannedavailability;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PlannedAvailabilityWriter {

    private final PlannedAvailabilityRepository plannedAvailabilityRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public PlannedAvailabilityWriter(
            PlannedAvailabilityRepository plannedAvailabilityRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.plannedAvailabilityRepository = plannedAvailabilityRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<PlannedAvailability> items, RDFSource rdfSource) {
        plannedAvailabilityRepository.deleteAll();
        plannedAvailabilityRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
