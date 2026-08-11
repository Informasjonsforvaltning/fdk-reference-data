package no.fdk.referencedata.digdir.legalresourcetype;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LegalResourceTypeService implements HarvestableReferenceData {
    private final String dbSourceID = "legal-resource-types-source";

    private final LegalResourceTypeHarvester legalResourceTypeHarvester;

    private final LegalResourceTypeRepository legalResourceTypeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public LegalResourceTypeService(
            LegalResourceTypeHarvester legalResourceTypeHarvester,
            LegalResourceTypeRepository legalResourceTypeRepository,
            ReferenceDataServiceSupport support) {
        this.legalResourceTypeHarvester = legalResourceTypeHarvester;
        this.legalResourceTypeRepository = legalResourceTypeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(legalResourceTypeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(legalResourceTypeHarvester, legalResourceTypeRepository, dbSourceID, "legal-resource-type");
    }
}
