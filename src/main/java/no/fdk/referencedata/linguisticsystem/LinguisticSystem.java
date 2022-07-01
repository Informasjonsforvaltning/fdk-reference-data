package no.fdk.referencedata.linguisticsystem;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class LinguisticSystem {
    String uri;
    String code;
    Map<String, String> label;
}
