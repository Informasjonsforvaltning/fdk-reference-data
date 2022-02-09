package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Kommuner {
    List<Kommune> kommuner;
}
