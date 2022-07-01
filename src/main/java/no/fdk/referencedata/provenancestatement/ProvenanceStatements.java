package no.fdk.referencedata.provenancestatement;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProvenanceStatements {
    List<ProvenanceStatement> provenanceStatements;
}
