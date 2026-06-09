package no.fdk.referencedata.eu.highvaluecategories;

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
public class HighValueCategoryService {
    private final String dbSourceID = "high-value-categories-source";

    private final HighValueCategoriesHarvester highValueCategoriesHarvester;

    private final HighValueCategoryWriter highValueCategoryWriter;

    private final HighValueCategoryRepository highValueCategoryRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public HighValueCategoryService(
            HighValueCategoriesHarvester highValueCategoriesHarvester,
            HighValueCategoryRepository highValueCategoryRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            HighValueCategoryWriter highValueCategoryWriter) {
        this.highValueCategoriesHarvester = highValueCategoriesHarvester;
        this.highValueCategoryRepository = highValueCategoryRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.highValueCategoryWriter = highValueCategoryWriter;
    }

    public boolean firstTime() {
        return highValueCategoryRepository.count() == 0;
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
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.HIGH_VALUE_CATEGORY.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.HIGH_VALUE_CATEGORY.name())
                            .latestVersion("0")
                            .build());

            final List<HighValueCategory> items = new ArrayList<>();
            highValueCategoriesHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} high-value categories", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(highValueCategoriesHarvester.getModel(), RDFFormat.TURTLE));

            settings.setLatestHarvestDate(LocalDateTime.now());
            settings.setLatestVersion(highValueCategoriesHarvester.getVersion());

            highValueCategoryWriter.replaceAll(items, rdfSource, settings);
        } catch (Exception e) {
            log.error("Unable to harvest high-value categories", e);
        }
    }
}
