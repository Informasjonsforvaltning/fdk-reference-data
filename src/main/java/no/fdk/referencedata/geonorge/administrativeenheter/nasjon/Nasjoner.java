package no.fdk.referencedata.geonorge.administrativeenheter.nasjon;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Nasjoner {
    List<Nasjon> nasjoner;
}
