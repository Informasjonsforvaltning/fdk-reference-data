package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.digdir.roletype.RoleType;
import no.fdk.referencedata.digdir.roletype.RoleTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class RoleTypeQuery implements GraphQLQueryResolver {

    @Autowired
    private RoleTypeRepository roleTypeRepository;

    public List<RoleType> getRoleTypes() {
        return StreamSupport.stream(roleTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(RoleType::getUri))
                .collect(Collectors.toList());
    }

    public RoleType getRoleTypeByCode(String code) {
        return roleTypeRepository.findByCode(code).orElse(null);
    }
}
