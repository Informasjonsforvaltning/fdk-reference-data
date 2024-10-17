package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.openlicences.OpenLicense;
import no.fdk.referencedata.openlicences.OpenLicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OpenLicenseQuery {

    private final OpenLicenseService openLicenseService;

    @Autowired
    public OpenLicenseQuery(OpenLicenseService openLicenseService) {
        this.openLicenseService = openLicenseService;
    }

    @QueryMapping
    public List<OpenLicense> openLicenses() {
        return openLicenseService.getAll().stream()
                .sorted(Comparator.comparing(OpenLicense::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public OpenLicense openLicenseByCode(@Argument String code) {
        return openLicenseService.getByCode(code).orElse(null);
    }
}
