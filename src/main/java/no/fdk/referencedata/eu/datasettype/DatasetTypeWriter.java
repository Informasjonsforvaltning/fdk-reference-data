package no.fdk.referencedata.eu.datasettype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DatasetTypeWriter {

    private final DatasetTypeRepository datasetTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;
    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public DatasetTypeWriter(
            DatasetTypeRepository datasetTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository) {
        this.datasetTypeRepository = datasetTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    @Transactional
    public void replaceAll(List<DatasetType> items, RDFSource rdfSource, HarvestSettings settings) {
        datasetTypeRepository.deleteAll();
        datasetTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
        harvestSettingsRepository.save(settings);
    }
}
