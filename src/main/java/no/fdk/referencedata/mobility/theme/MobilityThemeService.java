package no.fdk.referencedata.mobility.theme;

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
public class MobilityThemeService {
    private final String dbSourceID = "mobility-theme-source";

    private final MobilityThemeHarvester mobilityThemeHarvester;

    private final MobilityThemeRepository mobilityThemeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MobilityThemeService(MobilityThemeHarvester mobilityThemeHarvester,
                                MobilityThemeRepository mobilityThemeRepository,
                                RDFSourceRepository rdfSourceRepository,
                                HarvestSettingsRepository harvestSettingsRepository) {
        this.mobilityThemeHarvester = mobilityThemeHarvester;
        this.mobilityThemeRepository = mobilityThemeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return mobilityThemeRepository.count() == 0;
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
            final Version latestVersion = new Version(mobilityThemeHarvester.getVersion());

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.MOBILITY_THEME.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.MOBILITY_THEME.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion());

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                mobilityThemeRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<MobilityTheme> iterable = mobilityThemeHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} mobility themes", counter.get());
                mobilityThemeRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(mobilityThemeHarvester.getVersion());
                harvestSettingsRepository.save(settings);

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(mobilityThemeHarvester.getModel(), RDFFormat.TURTLE));
                rdfSourceRepository.save(rdfSource);
            }

        } catch(Exception e) {
            log.error("Unable to harvest mobility themes", e);
        }
    }
}
