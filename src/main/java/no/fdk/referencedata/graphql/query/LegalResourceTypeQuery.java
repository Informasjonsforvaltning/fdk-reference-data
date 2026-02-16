package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceType;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class LegalResourceTypeQuery {

    @Autowired
    private LegalResourceTypeRepository legalResourceTypeRepository;

    @QueryMapping
    public List<LegalResourceType> legalResourceTypes() {
        return StreamSupport.stream(legalResourceTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(LegalResourceType::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public LegalResourceType legalResourceTypeByCode(@Argument String code) {
        return legalResourceTypeRepository.findByCode(code).orElse(null);
    }
}
