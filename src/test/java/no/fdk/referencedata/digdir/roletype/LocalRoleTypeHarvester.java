package no.fdk.referencedata.digdir.roletype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalRoleTypeHarvester extends RoleTypeHarvester {
    private final String version;

    public LocalRoleTypeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("role-type.ttl");
    }
}
