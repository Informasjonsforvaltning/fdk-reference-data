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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MobilityDataStandardService {
    private final String dbSourceID = "mobility-data-standard-source";

    private final MobilityDataStandardHarvester mobilityDataStandardHarvester;

    private final MobilityDataStandardWriter mobilityDataStandardWriter;

    private final MobilityDataStandardRepository mobilityDataStandardRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MobilityDataStandardService(
            MobilityDataStandardHarvester mobilityDataStandardHarvester,
            MobilityDataStandardRepository mobilityDataStandardRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            MobilityDataStandardWriter mobilityDataStandardWriter) {
        this.mobilityDataStandardHarvester = mobilityDataStandardHarvester;
        this.mobilityDataStandardRepository = mobilityDataStandardRepository;
        this.mobilityDataStandardWriter = mobilityDataStandardWriter;
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

    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(mobilityDataStandardHarvester.getVersion());

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.MOBILITY_DATA_STANDARD.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.MOBILITY_DATA_STANDARD.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion());

            if (force || latestVersion.compareTo(currentVersion) > 0) {
                final List<MobilityDataStandard> items = new ArrayList<>();
                mobilityDataStandardHarvester.harvest().toIterable().forEach(items::add);
                log.info("Harvest and saving {} mobility data standards", items.size());

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(mobilityDataStandardHarvester.getModel(), RDFFormat.TURTLE));

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(mobilityDataStandardHarvester.getVersion());

                mobilityDataStandardWriter.replaceAll(items, rdfSource, settings);
            }

        } catch (Exception e) {
            log.error("Unable to harvest mobility data standards", e);
        }
    }
}
