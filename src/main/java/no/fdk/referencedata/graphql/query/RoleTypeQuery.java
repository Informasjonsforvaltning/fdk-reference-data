package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.digdir.roletype.RoleType;
import no.fdk.referencedata.digdir.roletype.RoleTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class RoleTypeQuery {

    @Autowired
    private RoleTypeRepository roleTypeRepository;

    @QueryMapping
    public List<RoleType> roleTypes() {
        return StreamSupport.stream(roleTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(RoleType::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public RoleType roleTypeByCode(@Argument String code) {
        return roleTypeRepository.findByCode(code).orElse(null);
    }
}
