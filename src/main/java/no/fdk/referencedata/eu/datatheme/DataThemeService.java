package no.fdk.referencedata.eu.datatheme;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DataThemeService {
    private final String dbSourceID = "data-theme-source";

    private final DataThemeHarvester dataThemeHarvester;

    private final DataThemeWriter dataThemeWriter;

    private final DataThemeRepository dataThemeRepository;

    private final RDFSourceRepository rdfSourceRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public DataThemeService(
            DataThemeHarvester dataThemeHarvester,
            DataThemeRepository dataThemeRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            DataThemeWriter dataThemeWriter) {
        this.dataThemeHarvester = dataThemeHarvester;
        this.dataThemeRepository = dataThemeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.dataThemeWriter = dataThemeWriter;
    }

    public boolean firstTime() {
        return dataThemeRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(dbSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(dataThemeHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.DATA_THEME.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.DATA_THEME.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if (force || latestVersion.compareTo(currentVersion) > 0) {
                final List<DataTheme> items = new ArrayList<>();
                dataThemeHarvester.harvest().toIterable().forEach(items::add);
                log.info("Harvest and saving {} data-themes", items.size());

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(dataThemeHarvester.getModel(), RDFFormat.TURTLE));

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(dataThemeHarvester.getVersion());

                dataThemeWriter.replaceAll(items, rdfSource, settings);
            }

        } catch (Exception e) {
            log.error("Unable to harvest data-themes", e);
        }
    }
}
