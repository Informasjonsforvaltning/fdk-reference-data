package no.fdk.referencedata.digdir.qualitydimension;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QualityDimensionService implements HarvestableReferenceData {
    private final String dbSourceID = "quality-dimensions-source";

    private final QualityDimensionHarvester qualityDimensionHarvester;

    private final QualityDimensionRepository qualityDimensionRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public QualityDimensionService(
            QualityDimensionHarvester qualityDimensionHarvester,
            QualityDimensionRepository qualityDimensionRepository,
            ReferenceDataServiceSupport support) {
        this.qualityDimensionHarvester = qualityDimensionHarvester;
        this.qualityDimensionRepository = qualityDimensionRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(qualityDimensionRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(qualityDimensionHarvester, qualityDimensionRepository, dbSourceID, "quality-dimension");
    }
}
