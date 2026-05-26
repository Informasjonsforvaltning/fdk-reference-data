package no.fdk.referencedata.eu.plannedavailability;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PlannedAvailabilityWriter {

    private final PlannedAvailabilityRepository plannedAvailabilityRepository;
    private final RDFSourceRepository rdfSourceRepository;
    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public PlannedAvailabilityWriter(
            PlannedAvailabilityRepository plannedAvailabilityRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository) {
        this.plannedAvailabilityRepository = plannedAvailabilityRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    @Transactional
    public void replaceAll(List<PlannedAvailability> items, RDFSource rdfSource, HarvestSettings settings) {
        plannedAvailabilityRepository.deleteAll();
        plannedAvailabilityRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
        harvestSettingsRepository.save(settings);
    }
}
