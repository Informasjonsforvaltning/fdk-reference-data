package no.fdk.referencedata.geonorge.administrativeenheter;

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
@Table(name = "enhet_varianter")
public class EnhetVariant {
    @Id
    @Column(name = "uri")
    String uri;

    @Column(name = "name")
    String name;

    @Column(name = "code")
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
