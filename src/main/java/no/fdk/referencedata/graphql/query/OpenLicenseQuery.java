package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.openlicences.OpenLicense;
import no.fdk.referencedata.openlicences.OpenLicenseService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OpenLicenseQuery {

    private final OpenLicenseService openLicenseService;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<OpenLicense> openLicenses() {
        return support.allSortedByUri(openLicenseService.getAll(), OpenLicense::getUri);
    }

    @QueryMapping
    public OpenLicense openLicenseByCode(@Argument String code) {
        return support.byCode(openLicenseService::getByCode, code);
    }
}
