package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.adms.publishertype.PublisherType;
import no.fdk.referencedata.adms.publishertype.PublisherTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PublisherTypeQuery implements GraphQLQueryResolver {

    private final PublisherTypeService publisherTypeService;

    @Autowired
    public PublisherTypeQuery(PublisherTypeService publisherTypeService) {
        this.publisherTypeService = publisherTypeService;
    }

    public List<PublisherType> getPublisherTypes() {
        return publisherTypeService.getAll().stream()
                .sorted(Comparator.comparing(PublisherType::getUri))
                .collect(Collectors.toList());
    }

    public PublisherType getPublisherTypeByCode(String code) {
        return publisherTypeService.getByCode(code).orElse(null);
    }
}
