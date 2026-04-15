package no.fdk.referencedata.ssb.fylkeorganisasjoner;

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
@Table(name = "fylke_organisasjoner")
public class FylkeOrganisasjon {
    @Id
    @Column(name = "uri")
    String uri;

    @Column(name = "organisasjonsnummer")
    String organisasjonsnummer;

    @Column(name = "organisasjonsnavn")
    String organisasjonsnavn;

    @Column(name = "fylkesnavn")
    String fylkesnavn;

    @Column(name = "fylkesnummer")
    String fylkesnummer;
}
