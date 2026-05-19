package no.fdk.referencedata.eu.continent;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Continents {
    List<Continent> continents;
}
