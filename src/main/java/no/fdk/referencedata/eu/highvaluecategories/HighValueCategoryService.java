package no.fdk.referencedata.eu.highvaluecategories;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HighValueCategoryService implements HarvestableReferenceData {
    private final String dbSourceID = "high-value-categories-source";

    private final HighValueCategoriesHarvester highValueCategoriesHarvester;

    private final HighValueCategoryRepository highValueCategoryRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public HighValueCategoryService(
            HighValueCategoriesHarvester highValueCategoriesHarvester,
            HighValueCategoryRepository highValueCategoryRepository,
            ReferenceDataServiceSupport support) {
        this.highValueCategoriesHarvester = highValueCategoriesHarvester;
        this.highValueCategoryRepository = highValueCategoryRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(highValueCategoryRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(highValueCategoriesHarvester, highValueCategoryRepository, dbSourceID, "high-value-category");
    }
}
