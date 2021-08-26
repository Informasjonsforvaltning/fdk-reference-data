package no.fdk.referencedata.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import no.fdk.referencedata.eu.accessright.AccessRight;
import no.fdk.referencedata.eu.accessright.AccessRightRepository;
import no.fdk.referencedata.eu.datatheme.DataTheme;
import no.fdk.referencedata.eu.datatheme.DataThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class AccessRightQuery implements GraphQLQueryResolver {

    @Autowired
    private AccessRightRepository accessRightRepository;

    public List<AccessRight> getAccessRights() {
        return StreamSupport.stream(accessRightRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(AccessRight::getUri))
                .collect(Collectors.toList());
    }

    public AccessRight getAccessRightByCode(String code) {
        return accessRightRepository.findByCode(code).orElse(null);
    }
}