package no.fdk.referencedata.mobility.conditions;

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
public class MobilityConditionService {
    private final String dbSourceID = "mobility-condition-source";

    private final MobilityConditionHarvester mobilityConditionHarvester;

    private final MobilityConditionWriter mobilityConditionWriter;

    private final MobilityConditionRepository mobilityConditionRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MobilityConditionService(
            MobilityConditionHarvester mobilityConditionHarvester,
            MobilityConditionRepository mobilityConditionRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            MobilityConditionWriter mobilityConditionWriter) {
        this.mobilityConditionHarvester = mobilityConditionHarvester;
        this.mobilityConditionRepository = mobilityConditionRepository;
        this.mobilityConditionWriter = mobilityConditionWriter;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return mobilityConditionRepository.count() == 0;
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
            final Version latestVersion = new Version(mobilityConditionHarvester.getVersion());

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.MOBILITY_CONDITION.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.MOBILITY_CONDITION.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion());

            if (force || latestVersion.compareTo(currentVersion) > 0) {
                final List<MobilityCondition> items = new ArrayList<>();
                mobilityConditionHarvester.harvest().toIterable().forEach(items::add);
                log.info("Harvest and saving {} mobility conditions", items.size());

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(mobilityConditionHarvester.getModel(), RDFFormat.TURTLE));

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(mobilityConditionHarvester.getVersion());

                mobilityConditionWriter.replaceAll(items, rdfSource, settings);
            }

        } catch (Exception e) {
            log.error("Unable to harvest mobility conditions", e);
        }
    }
}
