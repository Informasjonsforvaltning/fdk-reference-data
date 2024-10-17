package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceType;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class RelationshipWithSourceTypeQuery {

    @Autowired
    private RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;

    @QueryMapping
    public List<RelationshipWithSourceType> relationshipWithSourceTypes() {
        return StreamSupport.stream(relationshipWithSourceTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(RelationshipWithSourceType::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public RelationshipWithSourceType relationshipWithSourceTypeByCode(@Argument String code) {
        return relationshipWithSourceTypeRepository.findByCode(code).orElse(null);
    }
}
