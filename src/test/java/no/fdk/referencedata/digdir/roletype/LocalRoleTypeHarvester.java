package no.fdk.referencedata.digdir.roletype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalRoleTypeHarvester extends RoleTypeHarvester {

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("role-type.ttl");
    }
}
