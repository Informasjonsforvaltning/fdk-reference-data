package no.fdk.referencedata.search;

import java.util.stream.Stream;

public interface SearchableReferenceData {
    Stream<SearchHit> search(String query);
    SearchAlternative getSearchType();
}
