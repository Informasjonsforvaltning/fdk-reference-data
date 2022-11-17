package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelType;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ServiceChannelTypeQuery implements GraphQLQueryResolver {

    @Autowired
    private ServiceChannelTypeRepository serviceChannelTypeRepository;

    public List<ServiceChannelType> getServiceChannelTypes() {
        return StreamSupport.stream(serviceChannelTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(ServiceChannelType::getUri))
                .collect(Collectors.toList());
    }

    public ServiceChannelType getServiceChannelTypeByCode(String code) {
        return serviceChannelTypeRepository.findByCode(code).orElse(null);
    }
}
