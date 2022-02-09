package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Fylker {
    List<Fylke> fylker;
}
