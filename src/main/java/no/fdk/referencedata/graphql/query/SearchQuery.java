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
import java.util.stream.Collectors;

@Controller
public class SearchQuery {

    private final List<SearchableReferenceData> searchables;

    @Autowired
    public SearchQuery(List<SearchableReferenceData> searchables) {
        this.searchables = searchables;
    }

    @QueryMapping
    public List<SearchHit> search(@Argument SearchRequest req) {
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
