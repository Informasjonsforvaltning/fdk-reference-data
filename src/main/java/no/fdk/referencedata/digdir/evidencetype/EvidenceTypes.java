package no.fdk.referencedata.digdir.evidencetype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EvidenceTypes {
    List<EvidenceType> evidenceTypes;
}
