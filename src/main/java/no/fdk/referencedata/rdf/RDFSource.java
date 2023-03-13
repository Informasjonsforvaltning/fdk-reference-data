package no.fdk.referencedata.rdf;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class RDFSource {
    @Id
    String id;
    String turtle;
}
