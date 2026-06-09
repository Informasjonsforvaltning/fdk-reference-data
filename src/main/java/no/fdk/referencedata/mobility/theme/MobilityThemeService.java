package no.fdk.referencedata.mobility.theme;

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
public class MobilityThemeService {
    private final String dbSourceID = "mobility-theme-source";

    private final MobilityThemeHarvester mobilityThemeHarvester;

    private final MobilityThemeWriter mobilityThemeWriter;

    private final MobilityThemeRepository mobilityThemeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MobilityThemeService(
            MobilityThemeHarvester mobilityThemeHarvester,
            MobilityThemeRepository mobilityThemeRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            MobilityThemeWriter mobilityThemeWriter) {
        this.mobilityThemeHarvester = mobilityThemeHarvester;
        this.mobilityThemeRepository = mobilityThemeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.mobilityThemeWriter = mobilityThemeWriter;
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

    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.MOBILITY_THEME.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.MOBILITY_THEME.name())
                            .build());

            final List<MobilityTheme> items = new ArrayList<>();
            mobilityThemeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} mobility themes", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(mobilityThemeHarvester.getModel(), RDFFormat.TURTLE));

            settings.setLatestHarvestDate(LocalDateTime.now());

            mobilityThemeWriter.replaceAll(items, rdfSource, settings);
        } catch (Exception e) {
            log.error("Unable to harvest mobility themes", e);
        }
    }
}
