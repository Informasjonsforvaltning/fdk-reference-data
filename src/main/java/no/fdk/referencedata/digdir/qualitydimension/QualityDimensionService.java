package no.fdk.referencedata.digdir.qualitydimension;

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
public class QualityDimensionService {
    private final String dbSourceID = "quality-dimensions-source";

    private final QualityDimensionHarvester qualityDimensionHarvester;

    private final QualityDimensionWriter qualityDimensionWriter;

    private final QualityDimensionRepository qualityDimensionRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public QualityDimensionService(
            QualityDimensionHarvester qualityDimensionHarvester,
            QualityDimensionRepository qualityDimensionRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            QualityDimensionWriter qualityDimensionWriter) {
        this.qualityDimensionHarvester = qualityDimensionHarvester;
        this.qualityDimensionRepository = qualityDimensionRepository;
        this.qualityDimensionWriter = qualityDimensionWriter;
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

    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.QUALITY_DIMENSION.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.QUALITY_DIMENSION.name())
                            .build());

            final List<QualityDimension> items = new ArrayList<>();
            qualityDimensionHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} quality-dimensions", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(qualityDimensionHarvester.getModel(), RDFFormat.TURTLE));

            settings.setLatestHarvestDate(LocalDateTime.now());

            qualityDimensionWriter.replaceAll(items, rdfSource, settings);
        } catch (Exception e) {
            log.error("Unable to harvest quality-dimensions", e);
        }
    }
}
