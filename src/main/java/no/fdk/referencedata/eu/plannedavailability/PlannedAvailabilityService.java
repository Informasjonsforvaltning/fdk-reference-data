package no.fdk.referencedata.eu.plannedavailability;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlannedAvailabilityService implements HarvestableReferenceData {
    private final String dbSourceID = "planned-availability-source";

    private final PlannedAvailabilityHarvester plannedAvailabilityHarvester;

    private final PlannedAvailabilityRepository plannedAvailabilityRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public PlannedAvailabilityService(
            PlannedAvailabilityHarvester plannedAvailabilityHarvester,
            PlannedAvailabilityRepository plannedAvailabilityRepository,
            ReferenceDataServiceSupport support) {
        this.plannedAvailabilityHarvester = plannedAvailabilityHarvester;
        this.plannedAvailabilityRepository = plannedAvailabilityRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(plannedAvailabilityRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(plannedAvailabilityHarvester, plannedAvailabilityRepository, dbSourceID, "planned availabilities");
    }
}
