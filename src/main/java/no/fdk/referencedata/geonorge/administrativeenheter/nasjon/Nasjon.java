package no.fdk.referencedata.geonorge.administrativeenheter.nasjon;

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
public class Nasjon {
    @Id
    String uri;
    String nasjonsnavn;
    @Indexed
    String nasjonsnummer;

    public SearchHit toSearchHit() {
        return SearchHit.builder()
                .uri(this.uri)
                .code(this.nasjonsnummer)
                .label(Map.of("nb", this.nasjonsnavn))
                .type(SearchAlternative.ADMINISTRATIVE_ENHETER)
                .build();
    }
}
