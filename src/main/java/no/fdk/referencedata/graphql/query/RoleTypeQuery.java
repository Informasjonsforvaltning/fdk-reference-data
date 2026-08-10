package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.digdir.roletype.RoleType;
import no.fdk.referencedata.digdir.roletype.RoleTypeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RoleTypeQuery {

    private final RoleTypeRepository roleTypeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<RoleType> roleTypes() {
        return support.allSortedByUri(roleTypeRepository, RoleType::getUri);
    }

    @QueryMapping
    public RoleType roleTypeByCode(@Argument String code) {
        return support.byCode(roleTypeRepository::findByCode, code);
    }
}
