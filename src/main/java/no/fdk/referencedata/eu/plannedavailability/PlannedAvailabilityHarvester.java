package no.fdk.referencedata.eu.plannedavailability;

import no.fdk.referencedata.eu.GenericEuSkosHarvester;
import no.fdk.referencedata.eu.vocabulary.EUPlannedAvailability;
import no.fdk.referencedata.rdf.SkosMapper;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class PlannedAvailabilityHarvester extends GenericEuSkosHarvester<PlannedAvailability> {

    @Override
    protected String schemaName() {
        return "planned-availability";
    }

    @Override
    protected Resource scheme() {
        return EUPlannedAvailability.SCHEME;
    }

    @Override
    protected String logName() {
        return "planned availability";
    }

    @Override
    protected PlannedAvailability mapConcept(Resource plannedAvailability) {
        return PlannedAvailability.builder()
                .uri(plannedAvailability.getURI())
                .code(extractCode(plannedAvailability))
                .label(SkosMapper.extractLabels(plannedAvailability))
                .startUse(extractStartUse(plannedAvailability))
                .build();
    }
}
