package no.fdk.referencedata.provenancestatement;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ProvenanceStatement {
    String uri;
    String code;
    Map<String, String> label;
}
