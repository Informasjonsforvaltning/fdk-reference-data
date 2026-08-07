package no.fdk.referencedata.digdir.servicechanneltype;

import no.fdk.referencedata.core.ReferenceDataWriter;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ServiceChannelTypeService {
    private final String dbSourceID = "service-channel-types-source";

    private final ServiceChannelTypeHarvester serviceChannelTypeHarvester;

    private final ReferenceDataWriter referenceDataWriter;

    private final ServiceChannelTypeRepository serviceChannelTypeRepository;


    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ServiceChannelTypeService(
            ServiceChannelTypeHarvester serviceChannelTypeHarvester,
            ServiceChannelTypeRepository serviceChannelTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.serviceChannelTypeHarvester = serviceChannelTypeHarvester;
        this.serviceChannelTypeRepository = serviceChannelTypeRepository;
        this.referenceDataWriter = referenceDataWriter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return serviceChannelTypeRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(dbSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    public void harvestAndSave() {
        try {

            final List<ServiceChannelType> items = new ArrayList<>();
            serviceChannelTypeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} service-channel-types", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(serviceChannelTypeHarvester.getModel(), RDFFormat.TURTLE));


            referenceDataWriter.replaceAll(serviceChannelTypeRepository, items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest service-channel-types", e);
        }
    }
}
