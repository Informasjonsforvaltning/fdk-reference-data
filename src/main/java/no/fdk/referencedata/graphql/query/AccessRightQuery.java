package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.accessright.AccessRight;
import no.fdk.referencedata.eu.accessright.AccessRightRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AccessRightQuery {

    private final AccessRightRepository accessRightRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<AccessRight> accessRights() {
        return support.allSortedByUri(accessRightRepository, AccessRight::getUri);
    }

    @QueryMapping
    public AccessRight accessRightByCode(@Argument String code) {
        return support.byCode(accessRightRepository::findByCode, code);
    }
}
