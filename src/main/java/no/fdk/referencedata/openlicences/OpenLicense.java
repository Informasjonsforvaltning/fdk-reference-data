package no.fdk.referencedata.openlicences;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenLicense {
    String uri;
    String code;
    String isReplacedBy;
    Map<String, String> label;
}
