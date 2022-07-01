package no.fdk.referencedata.referencetypes;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReferenceTypes {
    List<ReferenceType> referenceTypes;
}
