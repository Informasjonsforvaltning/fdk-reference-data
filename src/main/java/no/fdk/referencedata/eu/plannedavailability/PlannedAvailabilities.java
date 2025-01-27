package no.fdk.referencedata.eu.plannedavailability;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlannedAvailabilities {
    List<PlannedAvailability> plannedAvailabilities;
}
