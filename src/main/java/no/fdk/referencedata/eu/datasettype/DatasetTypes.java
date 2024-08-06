package no.fdk.referencedata.eu.datasettype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DatasetTypes {
    List<DatasetType> datasetTypes;
}
