package no.fdk.referencedata.geonorge.administrativeenheter;

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
public class Enhet {
    @Id
    String uri;
    String name;
    @Indexed
    String code;

    public SearchHit toSearchHit() {
        return SearchHit.builder()
                .uri(this.uri)
                .code(this.code)
                .label(Map.of("nb", this.name))
                .type(SearchAlternative.ADMINISTRATIVE_ENHETER)
                .build();
    }
}
