package no.fdk.referencedata.eu.distributionstatus;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DistributionStatuses {
    List<DistributionStatus> distributionStatuses;
}
