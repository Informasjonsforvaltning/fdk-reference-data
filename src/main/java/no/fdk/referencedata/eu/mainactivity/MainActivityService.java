package no.fdk.referencedata.eu.mainactivity;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MainActivityService implements HarvestableReferenceData {
    private final String dbSourceID = "main-activities-source";

    private final MainActivityHarvester mainActivityHarvester;

    private final MainActivityRepository mainActivityRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public MainActivityService(
            MainActivityHarvester mainActivityHarvester,
            MainActivityRepository mainActivityRepository,
            ReferenceDataServiceSupport support) {
        this.mainActivityHarvester = mainActivityHarvester;
        this.mainActivityRepository = mainActivityRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(mainActivityRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(mainActivityHarvester, mainActivityRepository, dbSourceID, "main-activity");
    }
}
