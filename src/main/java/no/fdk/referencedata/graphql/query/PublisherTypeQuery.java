package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.adms.publishertype.PublisherType;
import no.fdk.referencedata.adms.publishertype.PublisherTypeService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PublisherTypeQuery {

    private final PublisherTypeService publisherTypeService;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<PublisherType> publisherTypes() {
        return support.allSortedByUri(publisherTypeService.getAll(), PublisherType::getUri);
    }

    @QueryMapping
    public PublisherType publisherTypeByCode(@Argument String code) {
        return support.byCode(publisherTypeService::getByCode, code);
    }
}
