package no.fdk.referencedata.mobility.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mobility_themes")
public class MobilityTheme {
    @Id
    @Column(name = "uri")
    String uri;

    @Column(name = "code")
    String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "label", columnDefinition = "jsonb")
    Map<String, String> label;
}
