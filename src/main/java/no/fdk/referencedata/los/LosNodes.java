package no.fdk.referencedata.los;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LosNodes {
    List<LosNode> losNodes;
}
