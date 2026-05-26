package no.fdk.referencedata.eu.continent;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ContinentWriter {

    private final ContinentRepository continentRepository;
    private final RDFSourceRepository rdfSourceRepository;
    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public ContinentWriter(
            ContinentRepository continentRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository) {
        this.continentRepository = continentRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    @Transactional
    public void replaceAll(List<Continent> items, RDFSource rdfSource, HarvestSettings settings) {
        continentRepository.deleteAll();
        continentRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
        harvestSettingsRepository.save(settings);
    }
}
