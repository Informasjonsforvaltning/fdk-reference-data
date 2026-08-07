package no.fdk.referencedata.eu.eurovoc;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EuroVocService implements HarvestableReferenceData {
    private final String dbSourceID = "eurovoc-source";

    private final EuroVocHarvester euroVocHarvester;

    private final EuroVocRepository euroVocRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public EuroVocService(
            EuroVocHarvester euroVocHarvester,
            EuroVocRepository euroVocRepository,
            ReferenceDataServiceSupport support) {
        this.euroVocHarvester = euroVocHarvester;
        this.euroVocRepository = euroVocRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(euroVocRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(euroVocHarvester, euroVocRepository, dbSourceID, "eurovocs");
    }
}
