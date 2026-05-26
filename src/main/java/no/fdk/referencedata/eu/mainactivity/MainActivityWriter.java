package no.fdk.referencedata.eu.mainactivity;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class MainActivityWriter {

    private final MainActivityRepository mainActivityRepository;
    private final RDFSourceRepository rdfSourceRepository;
    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public MainActivityWriter(
            MainActivityRepository mainActivityRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository) {
        this.mainActivityRepository = mainActivityRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    @Transactional
    public void replaceAll(List<MainActivity> items, RDFSource rdfSource, HarvestSettings settings) {
        mainActivityRepository.deleteAll();
        mainActivityRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
        harvestSettingsRepository.save(settings);
    }
}
