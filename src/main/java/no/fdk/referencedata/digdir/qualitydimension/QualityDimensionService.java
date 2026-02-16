package no.fdk.referencedata.digdir.qualitydimension;

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
public class QualityDimensionService {
    private final String dbSourceID = "quality-dimensions-source";

    private final QualityDimensionHarvester qualityDimensionHarvester;

    private final QualityDimensionRepository qualityDimensionRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public QualityDimensionService(QualityDimensionHarvester qualityDimensionHarvester,
                                   QualityDimensionRepository qualityDimensionRepository,
                                   RDFSourceRepository rdfSourceRepository,
                                   HarvestSettingsRepository harvestSettingsRepository) {
        this.qualityDimensionHarvester = qualityDimensionHarvester;
        this.qualityDimensionRepository = qualityDimensionRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return qualityDimensionRepository.count() == 0;
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
            final Version latestVersion = new Version(qualityDimensionHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.QUALITY_DIMENSION.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.QUALITY_DIMENSION.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                qualityDimensionRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<QualityDimension> iterable = qualityDimensionHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} quality-dimensions", counter.get());
                qualityDimensionRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(qualityDimensionHarvester.getVersion());
                harvestSettingsRepository.save(settings);

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(qualityDimensionHarvester.getModel(), RDFFormat.TURTLE));
                rdfSourceRepository.save(rdfSource);
            }

        } catch(Exception e) {
            log.error("Unable to harvest quality-dimensions", e);
        }
    }
}
