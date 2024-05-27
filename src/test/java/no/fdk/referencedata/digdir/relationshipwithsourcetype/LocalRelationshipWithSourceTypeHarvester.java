package no.fdk.referencedata.digdir.relationshipwithsourcetype;


import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalRelationshipWithSourceTypeHarvester extends RelationshipWithSourceTypeHarvester {
    private final String version;

    public LocalRelationshipWithSourceTypeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("relationship-with-source-type.ttl");
    }
}
