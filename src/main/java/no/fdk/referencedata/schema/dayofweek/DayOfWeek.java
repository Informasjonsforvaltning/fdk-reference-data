package no.fdk.referencedata.schema.dayofweek;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DayOfWeek {
    String uri;
    String code;
    Map<String, String> label;
}
