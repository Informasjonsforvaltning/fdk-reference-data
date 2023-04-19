package no.fdk.referencedata.eu.eurovoc;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Document
public class EuroVoc {
    @Id
    String uri;
    @Indexed
    String code;
    Map<String, String> label;

    List<URI> children;
    List<URI> parents;
    List<String> eurovocPaths;
}
