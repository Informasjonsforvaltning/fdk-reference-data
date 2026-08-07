package no.fdk.referencedata.eu.accessright;

import no.fdk.referencedata.eu.GenericEuSkosHarvester;
import no.fdk.referencedata.eu.vocabulary.EUAccessRight;
import no.fdk.referencedata.rdf.SkosMapper;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class AccessRightHarvester extends GenericEuSkosHarvester<AccessRight> {

    @Override
    protected String schemaName() {
        return "access-right";
    }

    @Override
    protected Resource scheme() {
        return EUAccessRight.SCHEME;
    }

    @Override
    protected String logName() {
        return "access-rights";
    }

    @Override
    protected AccessRight mapConcept(Resource accessRight) {
        return AccessRight.builder()
                .uri(accessRight.getURI())
                .code(extractCode(accessRight))
                .label(SkosMapper.extractLabels(accessRight))
                .build();
    }
}
