package no.fdk.referencedata.rdf;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rdf_sources")
public class RDFSource {
    @Id
    @Column(name = "id")
    String id;

    @Column(name = "turtle", columnDefinition = "text")
    String turtle;
}
