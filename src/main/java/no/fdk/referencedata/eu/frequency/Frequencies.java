package no.fdk.referencedata.eu.frequency;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Frequencies {
    List<Frequency> frequencies;
}
