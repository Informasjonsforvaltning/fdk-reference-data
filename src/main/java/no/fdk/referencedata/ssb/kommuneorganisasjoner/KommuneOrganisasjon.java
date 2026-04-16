package no.fdk.referencedata.ssb.kommuneorganisasjoner;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "kommune_organisasjoner")
public class KommuneOrganisasjon {
    @Id
    @Column(name = "uri")
    String uri;

    @Column(name = "organisasjonsnummer")
    String organisasjonsnummer;

    @Column(name = "organisasjonsnavn")
    String organisasjonsnavn;

    @Column(name = "kommunenavn")
    String kommunenavn;

    @Column(name = "kommunenummer")
    String kommunenummer;
}
