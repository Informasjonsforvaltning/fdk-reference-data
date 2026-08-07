package no.fdk.referencedata.eu.conceptstatus;

import no.fdk.referencedata.eu.GenericEuSkosHarvester;
import no.fdk.referencedata.eu.vocabulary.EUConceptStatus;
import no.fdk.referencedata.rdf.SkosMapper;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class ConceptStatusHarvester extends GenericEuSkosHarvester<ConceptStatus> {

    @Override
    protected String schemaName() {
        return "concept-status";
    }

    @Override
    protected Resource scheme() {
        return EUConceptStatus.SCHEME;
    }

    @Override
    protected String logName() {
        return "concept status";
    }

    @Override
    protected ConceptStatus mapConcept(Resource status) {
        return ConceptStatus.builder()
                .uri(status.getURI())
                .code(extractCode(status))
                .label(SkosMapper.extractLabels(status))
                .build();
    }
}
