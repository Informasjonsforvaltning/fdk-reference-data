package no.fdk.referencedata.digdir.relationshipwithsourcetype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalRelationshipWithSourceTypeHarvester extends RelationshipWithSourceTypeHarvester {

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("relationship-with-source-type.ttl");
    }
}
