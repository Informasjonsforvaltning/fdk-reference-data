package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceType;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class RelationshipWithSourceTypeQuery implements GraphQLQueryResolver {

    @Autowired
    private RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;

    public List<RelationshipWithSourceType> getRelationshipWithSourceTypes() {
        return StreamSupport.stream(relationshipWithSourceTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(RelationshipWithSourceType::getUri))
                .collect(Collectors.toList());
    }

    public RelationshipWithSourceType getRelationshipWithSourceTypeByCode(String code) {
        return relationshipWithSourceTypeRepository.findByCode(code).orElse(null);
    }
}
