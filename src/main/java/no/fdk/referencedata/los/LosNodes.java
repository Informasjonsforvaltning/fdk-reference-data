package no.fdk.referencedata.los;

import lombok.Builder;
import lombok.Data;
import no.fdk.referencedata.iana.mediatype.MediaType;

import java.util.List;

@Data
@Builder
public class LosNodes {
    List<LosNode> losNodes;
}
