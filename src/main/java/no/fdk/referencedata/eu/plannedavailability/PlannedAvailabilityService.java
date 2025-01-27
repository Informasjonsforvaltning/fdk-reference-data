package no.fdk.referencedata.eu.plannedavailability;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import no.fdk.referencedata.util.Version;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class PlannedAvailabilityService {
    private final String dbSourceID = "planned-availability-source";

    private final PlannedAvailabilityHarvester plannedAvailabilityHarvester;

    private final PlannedAvailabilityRepository plannedAvailabilityRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public PlannedAvailabilityService(PlannedAvailabilityHarvester plannedAvailabilityHarvester,
                                      PlannedAvailabilityRepository plannedAvailabilityRepository,
                                      RDFSourceRepository rdfSourceRepository,
                                      HarvestSettingsRepository harvestSettingsRepository) {
        this.plannedAvailabilityHarvester = plannedAvailabilityHarvester;
        this.plannedAvailabilityRepository = plannedAvailabilityRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return plannedAvailabilityRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(dbSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(plannedAvailabilityHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.PLANNED_AVAILABILITY.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.PLANNED_AVAILABILITY.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                plannedAvailabilityRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<PlannedAvailability> iterable = plannedAvailabilityHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} planned availabilities", counter.get());
                plannedAvailabilityRepository.saveAll(iterable);

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(plannedAvailabilityHarvester.getModel(), RDFFormat.TURTLE));
                rdfSourceRepository.save(rdfSource);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(plannedAvailabilityHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest planned availabilities", e);
        }
    }
}
