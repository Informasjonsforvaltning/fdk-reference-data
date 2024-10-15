package no.fdk.referencedata.search;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SearchHit {
    String uri;
    String code;
    Map<String, String> label;
    SearchAlternative type;
}
