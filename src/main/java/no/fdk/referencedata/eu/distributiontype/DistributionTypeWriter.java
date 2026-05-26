package no.fdk.referencedata.eu.distributiontype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DistributionTypeWriter {

    private final DistributionTypeRepository distributionTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;
    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public DistributionTypeWriter(
            DistributionTypeRepository distributionTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository) {
        this.distributionTypeRepository = distributionTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    @Transactional
    public void replaceAll(List<DistributionType> items, RDFSource rdfSource, HarvestSettings settings) {
        distributionTypeRepository.deleteAll();
        distributionTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
        harvestSettingsRepository.save(settings);
    }
}
