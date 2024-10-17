package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.search.FilterByURIsRequest;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchRequest;
import no.fdk.referencedata.search.SearchableReferenceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SearchQuery implements GraphQLQueryResolver {

    private final List<SearchableReferenceData> searchables;

    @Autowired
    public SearchQuery(List<SearchableReferenceData> searchables) {
        this.searchables = searchables;
    }

    public List<SearchHit> search(SearchRequest req) {
        String query = req.getQuery();

        if (query != null && query.length() > 1) {
            return searchables.stream()
                    .filter(searchable -> req.getTypes().contains(searchable.getSearchType()))
                    .flatMap(searchable -> searchable.search(query))
                    .sorted((c1, c2) -> c1.getLabel().get("nb").compareToIgnoreCase(c2.getLabel().get("nb")))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public List<SearchHit> filterByURIs(FilterByURIsRequest req) {
        List<String> uris = req.getUris();

        if (uris != null && !uris.isEmpty()) {
            return searchables.stream()
                    .filter(searchable -> req.getTypes().contains(searchable.getSearchType()))
                    .flatMap(searchable -> searchable.filterByURIs(uris))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
