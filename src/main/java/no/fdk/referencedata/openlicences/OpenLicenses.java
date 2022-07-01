package no.fdk.referencedata.openlicences;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OpenLicenses {
    List<OpenLicense> openLicenses;
}
