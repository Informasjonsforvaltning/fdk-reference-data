package no.fdk.referencedata.digdir.relationshipwithsourcetype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class RelationshipWithSourceTypeWriter {

    private final RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;
    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public RelationshipWithSourceTypeWriter(
            RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository) {
        this.relationshipWithSourceTypeRepository = relationshipWithSourceTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    @Transactional
    public void replaceAll(List<RelationshipWithSourceType> items, RDFSource rdfSource, HarvestSettings settings) {
        relationshipWithSourceTypeRepository.deleteAll();
        relationshipWithSourceTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
        harvestSettingsRepository.save(settings);
    }
}
