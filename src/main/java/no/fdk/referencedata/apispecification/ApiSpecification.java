package no.fdk.referencedata.apispecification;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Builder
public class ApiSpecification {
    String uri;
    String source;
    String code;
    Map<String, String> label;
}
