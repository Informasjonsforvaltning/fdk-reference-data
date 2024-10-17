package no.fdk.referencedata.search;

import java.util.List;
import java.util.stream.Stream;

public interface SearchableReferenceData {
    Stream<SearchHit> search(String query);
    Stream<SearchHit> filterByURIs(List<String> uris);
    SearchAlternative getSearchType();
}
