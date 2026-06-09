package no.fdk.referencedata.digdir.servicechanneltype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ServiceChannelTypeWriter {

    private final ServiceChannelTypeRepository serviceChannelTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ServiceChannelTypeWriter(
            ServiceChannelTypeRepository serviceChannelTypeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.serviceChannelTypeRepository = serviceChannelTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<ServiceChannelType> items, RDFSource rdfSource) {
        serviceChannelTypeRepository.deleteAll();
        serviceChannelTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
