package no.fdk.referencedata.eu.distributionstatus;

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
public class DistributionStatusService {
    private final String dbSourceID = "distribution-statuses-source";

    private final DistributionStatusHarvester distributionStatusHarvester;

    private final DistributionStatusWriter distributionStatusWriter;

    private final DistributionStatusRepository distributionStatusRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public DistributionStatusService(
            DistributionStatusHarvester distributionStatusHarvester,
            DistributionStatusRepository distributionStatusRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            DistributionStatusWriter distributionStatusWriter) {
        this.distributionStatusHarvester = distributionStatusHarvester;
        this.distributionStatusRepository = distributionStatusRepository;
        this.distributionStatusWriter = distributionStatusWriter;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return distributionStatusRepository.count() == 0;
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
            final Version latestVersion = new Version(distributionStatusHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.DISTRIBUTION_STATUS.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.DISTRIBUTION_STATUS.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if (force || latestVersion.compareTo(currentVersion) > 0) {
                final List<DistributionStatus> items = new ArrayList<>();
                distributionStatusHarvester.harvest().toIterable().forEach(items::add);
                log.info("Harvest and saving {} distribution statuses", items.size());

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(distributionStatusHarvester.getModel(), RDFFormat.TURTLE));

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(distributionStatusHarvester.getVersion());

                distributionStatusWriter.replaceAll(items, rdfSource, settings);
            }

        } catch (Exception e) {
            log.error("Unable to harvest distribution statuses", e);
        }
    }
}
