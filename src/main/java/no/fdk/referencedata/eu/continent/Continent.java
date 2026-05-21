package no.fdk.referencedata.eu.continent;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "continents")
public class Continent {
    @Id
    @Column(name = "uri")
    String uri;

    @Column(name = "code")
    String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "label", columnDefinition = "jsonb")
    Map<String, String> label;

    public SearchHit toSearchHit() {
        return SearchHit.builder()
                .uri(this.uri)
                .code(this.code)
                .label(this.label)
                .type(SearchAlternative.EU_LOCATIONS)
                .subType("CONTINENT")
                .build();
    }
}
