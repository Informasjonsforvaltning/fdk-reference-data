package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelType;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ServiceChannelTypeQuery {

    private final ServiceChannelTypeRepository serviceChannelTypeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<ServiceChannelType> serviceChannelTypes() {
        return support.allSortedByUri(serviceChannelTypeRepository, ServiceChannelType::getUri);
    }

    @QueryMapping
    public ServiceChannelType serviceChannelTypeByCode(@Argument String code) {
        return support.byCode(serviceChannelTypeRepository::findByCode, code);
    }
}
