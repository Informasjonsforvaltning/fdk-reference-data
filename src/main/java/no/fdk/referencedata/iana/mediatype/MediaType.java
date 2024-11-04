package no.fdk.referencedata.iana.mediatype;

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
public class MediaType {
    @Id
    String uri;
    String name;
    @Indexed
    String type;
    @Indexed
    String subType;

    public SearchHit toSearchHit() {
        return SearchHit.builder()
                .uri(this.uri)
                .code(this.type + "/" + this.subType)
                .label(Map.of("en", this.name))
                .type(SearchAlternative.IANA_MEDIA_TYPES)
                .build();
    }
}
