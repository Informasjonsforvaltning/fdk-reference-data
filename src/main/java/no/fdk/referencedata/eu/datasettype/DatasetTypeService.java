package no.fdk.referencedata.eu.datasettype;

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
public class DatasetTypeService {
    private final String dbSourceID = "dataset-types-source";

    private final DatasetTypeHarvester datasetTypeHarvester;

    private final DatasetTypeWriter datasetTypeWriter;

    private final DatasetTypeRepository datasetTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public DatasetTypeService(
            DatasetTypeHarvester datasetTypeHarvester,
            DatasetTypeRepository datasetTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            DatasetTypeWriter datasetTypeWriter) {
        this.datasetTypeHarvester = datasetTypeHarvester;
        this.datasetTypeRepository = datasetTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.datasetTypeWriter = datasetTypeWriter;
    }

    public boolean firstTime() {
        return datasetTypeRepository.count() == 0;
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
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.DATASET_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.DATASET_TYPE.name())
                            .build());

            final List<DatasetType> items = new ArrayList<>();
            datasetTypeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} dataset-types", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(datasetTypeHarvester.getModel(), RDFFormat.TURTLE));

            settings.setLatestHarvestDate(LocalDateTime.now());

            datasetTypeWriter.replaceAll(items, rdfSource, settings);
        } catch (Exception e) {
            log.error("Unable to harvest dataset-types", e);
        }
    }
}
