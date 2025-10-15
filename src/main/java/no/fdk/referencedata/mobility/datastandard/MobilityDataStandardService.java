package no.fdk.referencedata.mobility.datastandard;

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
public class MobilityDataStandardService {
    private final String dbSourceID = "mobility-data-standard-source";

    private final MobilityDataStandardHarvester mobilityDataStandardHarvester;

    private final MobilityDataStandardRepository mobilityDataStandardRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MobilityDataStandardService(MobilityDataStandardHarvester mobilityDataStandardHarvester,
                                       MobilityDataStandardRepository mobilityDataStandardRepository,
                                       RDFSourceRepository rdfSourceRepository,
                                       HarvestSettingsRepository harvestSettingsRepository) {
        this.mobilityDataStandardHarvester = mobilityDataStandardHarvester;
        this.mobilityDataStandardRepository = mobilityDataStandardRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return mobilityDataStandardRepository.count() == 0;
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
            final Version latestVersion = new Version(mobilityDataStandardHarvester.getVersion());

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.MOBILITY_DATA_STANDARD.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.MOBILITY_DATA_STANDARD.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion());

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                mobilityDataStandardRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<MobilityDataStandard> iterable = mobilityDataStandardHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} mobility data standards", counter.get());
                mobilityDataStandardRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(mobilityDataStandardHarvester.getVersion());
                harvestSettingsRepository.save(settings);

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(mobilityDataStandardHarvester.getModel(), RDFFormat.TURTLE));
                rdfSourceRepository.save(rdfSource);
            }

        } catch(Exception e) {
            log.error("Unable to harvest mobility data standards", e);
        }
    }
}
