package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

import lombok.Builder;
import lombok.Data;
import no.fdk.referencedata.search.SearchAlternative;
import no.fdk.referencedata.search.SearchHit;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Builder
@Document
public class Kommune {
    @Id
    String uri;
    String kommunenavn;
    String kommunenavnNorsk;
    @Indexed
    String kommunenummer;

    public SearchHit toSearchHit() {
        return SearchHit.builder()
                .uri(this.uri)
                .code(this.kommunenummer)
                .label(Map.of("nb", this.kommunenavnNorsk))
                .type(SearchAlternative.ADMINISTRATIVE_ENHETER)
                .build();
    }
}
