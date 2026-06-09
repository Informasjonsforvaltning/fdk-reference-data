package no.fdk.referencedata.eu.mainactivity;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MainActivityService {
    private final String dbSourceID = "main-activities-source";

    private final MainActivityHarvester mainActivityHarvester;

    private final MainActivityWriter mainActivityWriter;

    private final MainActivityRepository mainActivityRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MainActivityService(
            MainActivityHarvester mainActivityHarvester,
            MainActivityRepository mainActivityRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            MainActivityWriter mainActivityWriter) {
        this.mainActivityHarvester = mainActivityHarvester;
        this.mainActivityRepository = mainActivityRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.mainActivityWriter = mainActivityWriter;
    }

    public boolean firstTime() {
        return mainActivityRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(dbSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.MAIN_ACTIVITY.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.MAIN_ACTIVITY.name())
                            .latestVersion("0")
                            .build());

            final List<MainActivity> items = new ArrayList<>();
            mainActivityHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} main-activities", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(mainActivityHarvester.getModel(), RDFFormat.TURTLE));

            settings.setLatestHarvestDate(LocalDateTime.now());
            settings.setLatestVersion(mainActivityHarvester.getVersion());

            mainActivityWriter.replaceAll(items, rdfSource, settings);
        } catch (Exception e) {
            log.error("Unable to harvest main-activities", e);
        }
    }
}
