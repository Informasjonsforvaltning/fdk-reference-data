package no.fdk.referencedata.eu.filetype;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fdk.referencedata.search.SearchAlternative;
import no.fdk.referencedata.search.SearchHit;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file_types")
public class FileType {
    @Id
    @Column(name = "uri")
    String uri;

    @Column(name = "code")
    String code;

    @Column(name = "media_type")
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
