package no.fdk.referencedata.digdir.relationshipwithsourcetype;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelationshipWithSourceTypeService implements HarvestableReferenceData {
    private final String dbSourceID = "relationship-with-source-types-source";

    private final RelationshipWithSourceTypeHarvester relationshipWithSourceTypeHarvester;

    private final RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public RelationshipWithSourceTypeService(
            RelationshipWithSourceTypeHarvester relationshipWithSourceTypeHarvester,
            RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository,
            ReferenceDataServiceSupport support) {
        this.relationshipWithSourceTypeHarvester = relationshipWithSourceTypeHarvester;
        this.relationshipWithSourceTypeRepository = relationshipWithSourceTypeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(relationshipWithSourceTypeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(relationshipWithSourceTypeHarvester, relationshipWithSourceTypeRepository, dbSourceID, "relationship-with-source-types");
    }
}
