package no.fdk.referencedata.mobility.datastandard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MobilityDataStandards {
    List<MobilityDataStandard> mobilityDataStandards;
}
