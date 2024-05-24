package no.fdk.referencedata.digdir.relationshipWithSourceType;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RelationshipWithSourceTypes {
    List<RelationshipWithSourceType> relationshipWithSourceTypes;
}
