package no.fdk.referencedata.schema.dayofweek;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Builder
@Document
public class DayOfWeek {
    String uri;
    String code;
    Map<String, String> label;
}
