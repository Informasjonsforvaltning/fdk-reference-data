package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.search.FindByURIsRequest;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchRequest;
import no.fdk.referencedata.search.SearchableReferenceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class SearchQuery {

    private final List<SearchableReferenceData> searchables;

    @Autowired
    public SearchQuery(List<SearchableReferenceData> searchables) {
        this.searchables = searchables;
    }

    private String getPrioritizedLangLabel(Map<String, String> label) {
        if (label.containsKey("nb")) {
            return label.get("nb");
        }
        if (label.containsKey("no")) {
            return label.get("no");
        }
        if (label.containsKey("nn")) {
            return label.get("nn");
        }
        if (label.containsKey("en")) {
            return label.get("en");
        }
        return "";
    }

    private int compareLabelsForSort(String query, Map<String, String> labelMapA, Map<String, String> labelMapB) {
        String labelA = getPrioritizedLangLabel(labelMapA);
        String labelB = getPrioritizedLangLabel(labelMapB);

        boolean aStartsWithQuery = labelA.toLowerCase()
                .startsWith(query.toLowerCase());

        boolean bStartsWithQuery = labelB.toLowerCase()
                .startsWith(query.toLowerCase());

        if (aStartsWithQuery && !bStartsWithQuery) {
            return -1;
        }

        if (!aStartsWithQuery && bStartsWithQuery) {
            return 1;
        }

        return labelA.compareToIgnoreCase(labelB);
    }

    @QueryMapping
    public List<SearchHit> search(@Argument SearchRequest req) {
        String query = req.getQuery();

        if (query != null && query.length() > 1) {
            return searchables.stream()
                    .filter(searchable -> req.getTypes().contains(searchable.getSearchType()))
                    .flatMap(searchable -> searchable.search(query))
                    .sorted((c1, c2) -> compareLabelsForSort(query, c1.getLabel(), c2.getLabel()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @QueryMapping
    public List<SearchHit> findByURIs(@Argument FindByURIsRequest req) {
        List<String> uris = req.getUris();

        if (uris != null && !uris.isEmpty()) {
            return searchables.stream()
                    .filter(searchable -> req.getTypes().contains(searchable.getSearchType()))
                    .flatMap(searchable -> searchable.findByURIs(uris))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
