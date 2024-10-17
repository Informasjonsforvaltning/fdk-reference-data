package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.adms.status.ADMSStatus;
import no.fdk.referencedata.adms.status.ADMSStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ADMSStatusQuery {

    private final ADMSStatusService admsStatusService;

    @Autowired
    public ADMSStatusQuery(ADMSStatusService admsStatusService) {
        this.admsStatusService = admsStatusService;
    }

    @QueryMapping
    public List<ADMSStatus> statuses() {
        return admsStatusService.getAll().stream()
                .sorted(Comparator.comparing(ADMSStatus::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public ADMSStatus statusByCode(@Argument String code) {
        return admsStatusService.getByCode(code).orElse(null);
    }
}
