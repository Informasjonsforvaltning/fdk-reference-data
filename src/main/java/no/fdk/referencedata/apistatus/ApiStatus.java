package no.fdk.referencedata.apistatus;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ApiStatus {
    String uri;
    String code;
    Map<String, String> label;
}
