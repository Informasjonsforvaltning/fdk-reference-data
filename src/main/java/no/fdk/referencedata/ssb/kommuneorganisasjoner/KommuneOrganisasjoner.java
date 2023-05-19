package no.fdk.referencedata.ssb.kommuneorganisasjoner;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KommuneOrganisasjoner {
    List<KommuneOrganisasjon> kommuneOrganisasjoner;
}
