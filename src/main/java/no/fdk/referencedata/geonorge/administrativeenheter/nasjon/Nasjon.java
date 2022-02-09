package no.fdk.referencedata.geonorge.administrativeenheter.nasjon;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class Nasjon {
    @Id
    String uri;
    String nasjonsnavn;
    @Indexed
    String nasjonsnummer;
}
