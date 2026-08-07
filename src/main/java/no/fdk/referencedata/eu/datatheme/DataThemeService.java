package no.fdk.referencedata.eu.datatheme;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataThemeService implements HarvestableReferenceData {
    private final String dbSourceID = "data-theme-source";

    private final DataThemeHarvester dataThemeHarvester;

    private final DataThemeRepository dataThemeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public DataThemeService(
            DataThemeHarvester dataThemeHarvester,
            DataThemeRepository dataThemeRepository,
            ReferenceDataServiceSupport support) {
        this.dataThemeHarvester = dataThemeHarvester;
        this.dataThemeRepository = dataThemeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(dataThemeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(dataThemeHarvester, dataThemeRepository, dbSourceID, "data-themes");
    }
}
