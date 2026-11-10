package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.digdir.serviceoutputtype.ServiceOutputType;
import no.fdk.referencedata.digdir.serviceoutputtype.ServiceOutputTypeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ServiceOutputTypeQuery {

    private final ServiceOutputTypeRepository serviceOutputTypeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<ServiceOutputType> serviceOutputTypes() {
        return support.allSortedByUri(serviceOutputTypeRepository, ServiceOutputType::getUri);
    }

    @QueryMapping
    public ServiceOutputType serviceOutputTypeByCode(@Argument String code) {
        return support.byCode(serviceOutputTypeRepository::findByCode, code);
    }
}
