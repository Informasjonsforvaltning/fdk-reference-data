package no.fdk.referencedata.los;

import lombok.Builder;
import lombok.Data;
import no.fdk.referencedata.iana.mediatype.MediaType;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class LosNodes {
    List<LosNode> losNodes;
}
