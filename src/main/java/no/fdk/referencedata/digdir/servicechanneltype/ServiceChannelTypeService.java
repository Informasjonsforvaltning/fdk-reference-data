package no.fdk.referencedata.digdir.servicechanneltype;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceChannelTypeService implements HarvestableReferenceData {
    private final String dbSourceID = "service-channel-types-source";

    private final ServiceChannelTypeHarvester serviceChannelTypeHarvester;

    private final ServiceChannelTypeRepository serviceChannelTypeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public ServiceChannelTypeService(
            ServiceChannelTypeHarvester serviceChannelTypeHarvester,
            ServiceChannelTypeRepository serviceChannelTypeRepository,
            ReferenceDataServiceSupport support) {
        this.serviceChannelTypeHarvester = serviceChannelTypeHarvester;
        this.serviceChannelTypeRepository = serviceChannelTypeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(serviceChannelTypeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(serviceChannelTypeHarvester, serviceChannelTypeRepository, dbSourceID, "service-channel-types");
    }
}
