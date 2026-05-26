package no.fdk.referencedata.eu.accessright;

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
public class AccessRightService {
    private final String dbSourceID = "access-rights-source";

    private final AccessRightHarvester accessRightHarvester;

    private final AccessRightWriter accessRightWriter;

    private final AccessRightRepository accessRightRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public AccessRightService(
            AccessRightHarvester accessRightHarvester,
            AccessRightRepository accessRightRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            AccessRightWriter accessRightWriter) {
        this.accessRightHarvester = accessRightHarvester;
        this.accessRightRepository = accessRightRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.accessRightWriter = accessRightWriter;
    }

    public boolean firstTime() {
        return accessRightRepository.count() == 0;
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
            final Version latestVersion = new Version(accessRightHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.ACCESS_RIGHT.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.ACCESS_RIGHT.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if (force || latestVersion.compareTo(currentVersion) > 0) {
                final List<AccessRight> items = new ArrayList<>();
                accessRightHarvester.harvest().toIterable().forEach(items::add);
                log.info("Harvest and saving {} access-rights", items.size());

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(accessRightHarvester.getModel(), RDFFormat.TURTLE));

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(accessRightHarvester.getVersion());

                accessRightWriter.replaceAll(items, rdfSource, settings);
            }

        } catch (Exception e) {
            log.error("Unable to harvest access-rights", e);
        }
    }
}
