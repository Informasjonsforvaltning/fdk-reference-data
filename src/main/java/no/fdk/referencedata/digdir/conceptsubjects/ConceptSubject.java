package no.fdk.referencedata.digdir.conceptsubjects;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Builder
@Document
public class ConceptSubject {
    @Id
    String uri;
    String code;
    Map<String, String> label;
}
