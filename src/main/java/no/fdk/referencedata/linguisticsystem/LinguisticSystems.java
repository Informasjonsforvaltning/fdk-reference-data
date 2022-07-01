package no.fdk.referencedata.linguisticsystem;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LinguisticSystems {
    List<LinguisticSystem> linguisticSystems;
}
