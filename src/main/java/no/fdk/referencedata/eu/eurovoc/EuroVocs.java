package no.fdk.referencedata.eu.eurovoc;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EuroVocs {
    List<EuroVoc> euroVocs;
}
