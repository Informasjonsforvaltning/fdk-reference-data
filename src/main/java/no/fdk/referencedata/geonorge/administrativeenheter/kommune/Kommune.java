package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class Kommune {
    @Id
    String uri;
    String kommunenavn;
    String kommunenavnNorsk;
    @Indexed
    String kommunenummer;
}
