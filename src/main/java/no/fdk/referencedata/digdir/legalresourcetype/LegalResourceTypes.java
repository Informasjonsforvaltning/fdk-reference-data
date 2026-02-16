package no.fdk.referencedata.digdir.legalresourcetype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LegalResourceTypes {
    List<LegalResourceType> legalResourceTypes;
}
