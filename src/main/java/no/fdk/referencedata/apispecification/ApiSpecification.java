package no.fdk.referencedata.apispecification;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ApiSpecification {
    String uri;
    String source;
    String code;
    Map<String, String> label;
}
