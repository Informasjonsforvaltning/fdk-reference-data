package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.productstatus.ProductStatus;
import no.fdk.referencedata.eu.productstatus.ProductStatusRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductStatusQuery {

    private final ProductStatusRepository productStatusRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<ProductStatus> productStatuses() {
        return support.allSortedByUri(productStatusRepository, ProductStatus::getUri);
    }

    @QueryMapping
    public ProductStatus productStatusByCode(@Argument String code) {
        return support.byCode(productStatusRepository::findByCode, code);
    }
}
