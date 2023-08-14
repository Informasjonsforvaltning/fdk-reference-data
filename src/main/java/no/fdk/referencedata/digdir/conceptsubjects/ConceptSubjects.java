package no.fdk.referencedata.digdir.conceptsubjects;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConceptSubjects {
    List<ConceptSubject> conceptSubjects;
}
