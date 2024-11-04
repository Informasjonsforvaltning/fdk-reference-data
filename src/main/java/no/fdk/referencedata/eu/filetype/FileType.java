package no.fdk.referencedata.eu.filetype;

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
public class FileType {
    @Id
    String uri;
    @Indexed
    String code;
    String mediaType;

    public SearchHit toSearchHit() {
        return SearchHit.builder()
                .uri(this.uri)
                .code(this.code)
                .label(Map.of("en", this.code))
                .type(SearchAlternative.EU_FILE_TYPES)
                .build();
    }
}
