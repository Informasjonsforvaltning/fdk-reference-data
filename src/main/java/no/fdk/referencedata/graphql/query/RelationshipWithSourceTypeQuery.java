package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceType;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RelationshipWithSourceTypeQuery {

    private final RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<RelationshipWithSourceType> relationshipWithSourceTypes() {
        return support.allSortedByUri(relationshipWithSourceTypeRepository, RelationshipWithSourceType::getUri);
    }

    @QueryMapping
    public RelationshipWithSourceType relationshipWithSourceTypeByCode(@Argument String code) {
        return support.byCode(relationshipWithSourceTypeRepository::findByCode, code);
    }
}
