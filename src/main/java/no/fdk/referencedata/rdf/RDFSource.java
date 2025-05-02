package no.fdk.referencedata.rdf;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "rdfSources")
public class RDFSource {
    @Id
    String id;
    String turtle;
}
