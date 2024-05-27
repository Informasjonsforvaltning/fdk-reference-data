package no.fdk.referencedata.digdir.relationshipwithsourcetype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RelationshipWithSourceTypes {
    List<RelationshipWithSourceType> relationshipWithSourceTypes;
}
