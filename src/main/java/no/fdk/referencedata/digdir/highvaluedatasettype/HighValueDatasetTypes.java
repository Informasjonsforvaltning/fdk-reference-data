package no.fdk.referencedata.digdir.highvaluedatasettype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HighValueDatasetTypes {
    List<HighValueDatasetType> highValueDatasetTypes;
}
