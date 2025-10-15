package no.fdk.referencedata.mobility.conditions;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MobilityConditions {
    List<MobilityCondition> mobilityConditions;
}
