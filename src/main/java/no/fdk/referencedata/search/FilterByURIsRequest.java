package no.fdk.referencedata.search;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FilterByURIsRequest {
    private List<String> uris;
    private List<SearchAlternative> types;
}
