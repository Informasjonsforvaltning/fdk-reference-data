package no.fdk.referencedata.eu.accessright;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccessRightService implements HarvestableReferenceData {
    private final String dbSourceID = "access-rights-source";

    private final AccessRightHarvester accessRightHarvester;

    private final AccessRightRepository accessRightRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public AccessRightService(
            AccessRightHarvester accessRightHarvester,
            AccessRightRepository accessRightRepository,
            ReferenceDataServiceSupport support) {
        this.accessRightHarvester = accessRightHarvester;
        this.accessRightRepository = accessRightRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(accessRightRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(accessRightHarvester, accessRightRepository, dbSourceID, "access-right");
    }
}
