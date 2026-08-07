package no.fdk.referencedata.eu.distributionstatus;

import no.fdk.referencedata.eu.GenericEuSkosHarvester;
import no.fdk.referencedata.eu.vocabulary.EUDistributionStatus;
import no.fdk.referencedata.rdf.SkosMapper;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class DistributionStatusHarvester extends GenericEuSkosHarvester<DistributionStatus> {

    @Override
    protected String schemaName() {
        return "distribution-status";
    }

    @Override
    protected Resource scheme() {
        return EUDistributionStatus.SCHEME;
    }

    @Override
    protected String logName() {
        return "distribution statuses";
    }

    @Override
    protected DistributionStatus mapConcept(Resource distributionStatus) {
        return DistributionStatus.builder()
                .uri(distributionStatus.getURI())
                .code(extractCode(distributionStatus))
                .label(SkosMapper.extractLabels(distributionStatus))
                .startUse(extractStartUse(distributionStatus))
                .build();
    }
}
