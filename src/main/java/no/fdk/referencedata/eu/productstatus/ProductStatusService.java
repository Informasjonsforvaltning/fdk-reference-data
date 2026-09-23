package no.fdk.referencedata.eu.productstatus;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductStatusService implements HarvestableReferenceData {
    private final String dbSourceID = "product-statuses-source";

    private final ProductStatusHarvester productStatusHarvester;

    private final ProductStatusRepository productStatusRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public ProductStatusService(
            ProductStatusHarvester productStatusHarvester,
            ProductStatusRepository productStatusRepository,
            ReferenceDataServiceSupport support) {
        this.productStatusHarvester = productStatusHarvester;
        this.productStatusRepository = productStatusRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(productStatusRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(productStatusHarvester, productStatusRepository, dbSourceID, "product-status");
    }
}
