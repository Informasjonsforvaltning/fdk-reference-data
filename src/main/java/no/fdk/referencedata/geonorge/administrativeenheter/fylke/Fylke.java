package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class Fylke {
    @Id
    String uri;
    String fylkesnavn;
    @Indexed
    String fylkesnummer;
}
