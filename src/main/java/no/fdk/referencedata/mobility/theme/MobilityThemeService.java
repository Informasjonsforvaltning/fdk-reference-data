package no.fdk.referencedata.mobility.theme;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MobilityThemeService implements HarvestableReferenceData {
    private final String dbSourceID = "mobility-theme-source";

    private final MobilityThemeHarvester mobilityThemeHarvester;

    private final MobilityThemeRepository mobilityThemeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public MobilityThemeService(
            MobilityThemeHarvester mobilityThemeHarvester,
            MobilityThemeRepository mobilityThemeRepository,
            ReferenceDataServiceSupport support) {
        this.mobilityThemeHarvester = mobilityThemeHarvester;
        this.mobilityThemeRepository = mobilityThemeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(mobilityThemeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(mobilityThemeHarvester, mobilityThemeRepository, dbSourceID, "mobility-theme");
    }
}
