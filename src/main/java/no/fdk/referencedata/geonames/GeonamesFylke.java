package no.fdk.referencedata.geonames;

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
@Table(name = "geonames_fylker")
public class GeonamesFylke {
    @Id
    @Column(name = "uri")
    String uri;

    @Column(name = "geoname_id")
    String geonameId;

    @Column(name = "name")
    String name;

    public SearchHit toSearchHit() {
        return SearchHit.builder()
                .uri(this.uri)
                .code(this.geonameId)
                .label(this.name != null ? Map.of("no", this.name) : Map.of())
                .type(SearchAlternative.GEONAMES)
                .subType("FYLKE")
                .build();
    }
}
