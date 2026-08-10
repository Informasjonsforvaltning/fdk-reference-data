package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategory;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategoryRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HighValueCategoryQuery {

    private final HighValueCategoryRepository highValueCategoryRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<HighValueCategory> highValueCategories() {
        return support.allSortedByUri(highValueCategoryRepository, HighValueCategory::getUri);
    }

    @QueryMapping
    public HighValueCategory highValueCategoryByCode(@Argument String code) {
        return support.byCode(highValueCategoryRepository::findByCode, code);
    }
}
