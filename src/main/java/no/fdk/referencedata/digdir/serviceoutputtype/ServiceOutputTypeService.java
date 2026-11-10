package no.fdk.referencedata.digdir.serviceoutputtype;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceOutputTypeService implements HarvestableReferenceData {
    private final String dbSourceID = "service-output-types-source";

    private final ServiceOutputTypeHarvester serviceOutputTypeHarvester;

    private final ServiceOutputTypeRepository serviceOutputTypeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public ServiceOutputTypeService(
            ServiceOutputTypeHarvester serviceOutputTypeHarvester,
            ServiceOutputTypeRepository serviceOutputTypeRepository,
            ReferenceDataServiceSupport support) {
        this.serviceOutputTypeHarvester = serviceOutputTypeHarvester;
        this.serviceOutputTypeRepository = serviceOutputTypeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(serviceOutputTypeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(serviceOutputTypeHarvester, serviceOutputTypeRepository, dbSourceID, "service-output-type");
    }
}
