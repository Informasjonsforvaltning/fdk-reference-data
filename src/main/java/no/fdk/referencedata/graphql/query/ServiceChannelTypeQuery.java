package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelType;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class ServiceChannelTypeQuery {

    @Autowired
    private ServiceChannelTypeRepository serviceChannelTypeRepository;

    @QueryMapping
    public List<ServiceChannelType> serviceChannelTypes() {
        return StreamSupport.stream(serviceChannelTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(ServiceChannelType::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public ServiceChannelType serviceChannelTypeByCode(@Argument String code) {
        return serviceChannelTypeRepository.findByCode(code).orElse(null);
    }
}
