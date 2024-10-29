package no.fdk.referencedata.geonorge.administrativeenheter;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Enheter {
    List<Enhet> enheter;
}
