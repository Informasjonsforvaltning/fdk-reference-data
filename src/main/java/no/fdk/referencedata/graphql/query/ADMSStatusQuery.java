package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.adms.status.ADMSStatus;
import no.fdk.referencedata.adms.status.ADMSStatusService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ADMSStatusQuery {

    private final ADMSStatusService admsStatusService;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<ADMSStatus> statuses() {
        return support.allSortedByUri(admsStatusService.getAll(), ADMSStatus::getUri);
    }

    @QueryMapping
    public ADMSStatus statusByCode(@Argument String code) {
        return support.byCode(admsStatusService::getByCode, code);
    }
}
