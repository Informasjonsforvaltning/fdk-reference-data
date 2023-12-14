package no.fdk.referencedata.eu.frequency;

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
public class FrequencyService {
    private final String dbSourceID = "frequencies-source";

    private final FrequencyHarvester frequencyHarvester;

    private final FrequencyRepository frequencyRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public FrequencyService(FrequencyHarvester frequencyHarvester,
                            FrequencyRepository frequencyRepository,
                            RDFSourceRepository rdfSourceRepository,
                            HarvestSettingsRepository harvestSettingsRepository) {
        this.frequencyHarvester = frequencyHarvester;
        this.frequencyRepository = frequencyRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return frequencyRepository.count() == 0;
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
            final Version latestVersion = new Version(frequencyHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.FREQUENCY.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.FREQUENCY.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                frequencyRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<Frequency> iterable = frequencyHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} frequencies", counter.get());
                frequencyRepository.saveAll(iterable);

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(frequencyHarvester.getModel(), RDFFormat.TURTLE));
                rdfSourceRepository.save(rdfSource);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(frequencyHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest frequencies", e);
        }
    }
}
