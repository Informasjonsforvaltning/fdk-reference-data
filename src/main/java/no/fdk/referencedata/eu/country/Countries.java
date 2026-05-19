package no.fdk.referencedata.eu.country;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Countries {
    List<Country> countries;
}
