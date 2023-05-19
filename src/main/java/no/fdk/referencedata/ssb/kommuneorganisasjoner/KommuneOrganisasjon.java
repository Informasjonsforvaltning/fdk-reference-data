package no.fdk.referencedata.ssb.kommuneorganisasjoner;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class KommuneOrganisasjon {
    @Id
    String uri;
    @Indexed
    String organisasjonsnummer;
    String organisasjonsnavn;
    String kommunenavn;
    @Indexed
    String kommunenummer;
}
