package no.fdk.referencedata.eu.distributiontype;

import no.fdk.referencedata.eu.GenericEuSkosHarvester;
import no.fdk.referencedata.eu.vocabulary.EUDistributionType;
import no.fdk.referencedata.rdf.SkosMapper;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class DistributionTypeHarvester extends GenericEuSkosHarvester<DistributionType> {

    @Override
    protected String schemaName() {
        return "distribution-type";
    }

    @Override
    protected Resource scheme() {
        return EUDistributionType.SCHEME;
    }

    @Override
    protected String logName() {
        return "distribution types";
    }

    @Override
    protected DistributionType mapConcept(Resource distributionType) {
        return DistributionType.builder()
                .uri(distributionType.getURI())
                .code(extractCode(distributionType))
                .label(SkosMapper.extractLabels(distributionType))
                .startUse(extractStartUse(distributionType))
                .build();
    }
}
