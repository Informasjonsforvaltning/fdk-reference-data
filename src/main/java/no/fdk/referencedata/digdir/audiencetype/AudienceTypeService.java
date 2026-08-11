package no.fdk.referencedata.digdir.audiencetype;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AudienceTypeService implements HarvestableReferenceData {
    private final String dbSourceID = "audience-types-source";

    private final AudienceTypeHarvester audienceTypeHarvester;

    private final AudienceTypeRepository audienceTypeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public AudienceTypeService(
            AudienceTypeHarvester audienceTypeHarvester,
            AudienceTypeRepository audienceTypeRepository,
            ReferenceDataServiceSupport support) {
        this.audienceTypeHarvester = audienceTypeHarvester;
        this.audienceTypeRepository = audienceTypeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(audienceTypeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(audienceTypeHarvester, audienceTypeRepository, dbSourceID, "audience-type");
    }
}
