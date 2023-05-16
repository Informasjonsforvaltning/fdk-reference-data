package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class FylkeOrganisasjon {
    @Id
    String uri;
    @Indexed
    String organisasjonsnummer;
    String organisasjonsnavn;
    String fylkesnavn;
    @Indexed
    String fylkesnummer;
}
