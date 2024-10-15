package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

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
public class Fylke {
    @Id
    String uri;
    String fylkesnavn;
    @Indexed
    String fylkesnummer;

    public SearchHit toSearchHit() {
        return SearchHit.builder()
                .uri(this.uri)
                .code(this.fylkesnummer)
                .label(Map.of("nb", this.fylkesnavn))
                .type(SearchAlternative.ADMINISTRATIVE_ENHETER)
                .build();
    }
}
