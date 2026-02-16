package no.fdk.referencedata.digdir.qualitydimension;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QualityDimensions {
    List<QualityDimension> qualityDimensions;
}
