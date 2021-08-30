package no.fdk.referencedata.eu.datatheme;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@Document
public class ConceptSchema {
    @Id
    String uri;
    Map<String, String> label;
    String versionNumber;
}
