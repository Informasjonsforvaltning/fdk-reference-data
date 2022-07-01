package no.fdk.referencedata.referencetypes;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ReferenceType {
    String uri;
    String code;
    Map<String, String> label;
}
