package no.fdk.referencedata.eu.frequency;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class FrequencyWriter {

    private final FrequencyRepository frequencyRepository;
    private final RDFSourceRepository rdfSourceRepository;
    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public FrequencyWriter(
            FrequencyRepository frequencyRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository) {
        this.frequencyRepository = frequencyRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    @Transactional
    public void replaceAll(List<Frequency> items, RDFSource rdfSource, HarvestSettings settings) {
        frequencyRepository.deleteAll();
        frequencyRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
        harvestSettingsRepository.save(settings);
    }
}
