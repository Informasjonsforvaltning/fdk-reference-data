package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.highvaluecategories.HighValueCategory;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class HighValueCategoryQuery {

    @Autowired
    private HighValueCategoryRepository highValueCategoryRepository;

    @QueryMapping
    public List<HighValueCategory> highValueCategories() {
        return StreamSupport.stream(highValueCategoryRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(HighValueCategory::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public HighValueCategory highValueCategoryByCode(@Argument String code) {
        return highValueCategoryRepository.findByCode(code).orElse(null);
    }
}
