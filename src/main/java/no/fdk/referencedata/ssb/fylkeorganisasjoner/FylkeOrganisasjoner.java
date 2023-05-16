package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FylkeOrganisasjoner {
    List<FylkeOrganisasjon> fylkeOrganisasjoner;
}
