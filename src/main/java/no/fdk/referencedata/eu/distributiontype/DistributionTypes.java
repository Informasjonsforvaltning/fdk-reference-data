package no.fdk.referencedata.eu.distributiontype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DistributionTypes {
    List<DistributionType> distributionTypes;
}
