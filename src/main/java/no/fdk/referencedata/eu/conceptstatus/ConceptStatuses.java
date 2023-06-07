package no.fdk.referencedata.eu.conceptstatus;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConceptStatuses {
    List<ConceptStatus> conceptStatuses;
}
