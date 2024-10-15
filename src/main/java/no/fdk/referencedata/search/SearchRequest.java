package no.fdk.referencedata.search;

import lombok.*;

import java.util.List;

@Data
@Builder
public class SearchRequest {
    private String query;
    private List<SearchAlternative> types;
}
