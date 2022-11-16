package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.adms.status.ADMSStatus;
import no.fdk.referencedata.adms.status.ADMSStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ADMSStatusQuery implements GraphQLQueryResolver {

    private final ADMSStatusService admsStatusService;

    @Autowired
    public ADMSStatusQuery(ADMSStatusService admsStatusService) {
        this.admsStatusService = admsStatusService;
    }

    public List<ADMSStatus> getStatuses() {
        return admsStatusService.getAll().stream()
                .sorted(Comparator.comparing(ADMSStatus::getUri))
                .collect(Collectors.toList());
    }

    public ADMSStatus getStatusByCode(String code) {
        return admsStatusService.getByCode(code).orElse(null);
    }
}
