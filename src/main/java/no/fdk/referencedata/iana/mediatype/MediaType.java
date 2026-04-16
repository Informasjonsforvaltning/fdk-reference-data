package no.fdk.referencedata.iana.mediatype;

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
@Table(name = "media_types")
public class MediaType {
    @Id
    @Column(name = "uri")
    String uri;

    @Column(name = "name")
    String name;

    @Column(name = "type")
    String type;

    @Column(name = "sub_type")
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
