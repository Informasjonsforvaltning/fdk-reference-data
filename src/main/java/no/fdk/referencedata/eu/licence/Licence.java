package no.fdk.referencedata.eu.licence;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Document
public class Licence {
    @Id
    String uri;
    @Indexed
    String code;
    Map<String, String> label;
    Map<String, String> definition;
    Boolean deprecated;
    List<String> context;
}
