package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.accessright.AccessRight;
import no.fdk.referencedata.eu.accessright.AccessRightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class AccessRightQuery {

    @Autowired
    private AccessRightRepository accessRightRepository;

    @QueryMapping
    public List<AccessRight> accessRights() {
        return StreamSupport.stream(accessRightRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(AccessRight::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public AccessRight accessRightByCode(@Argument String code) {
        return accessRightRepository.findByCode(code).orElse(null);
    }
}
