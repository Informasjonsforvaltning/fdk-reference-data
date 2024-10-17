package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.adms.publishertype.PublisherType;
import no.fdk.referencedata.adms.publishertype.PublisherTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PublisherTypeQuery {

    private final PublisherTypeService publisherTypeService;

    @Autowired
    public PublisherTypeQuery(PublisherTypeService publisherTypeService) {
        this.publisherTypeService = publisherTypeService;
    }

    @QueryMapping
    public List<PublisherType> publisherTypes() {
        return publisherTypeService.getAll().stream()
                .sorted(Comparator.comparing(PublisherType::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public PublisherType publisherTypeByCode(@Argument String code) {
        return publisherTypeService.getByCode(code).orElse(null);
    }
}
