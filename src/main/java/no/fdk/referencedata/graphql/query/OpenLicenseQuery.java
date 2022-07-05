package no.fdk.referencedata.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import no.fdk.referencedata.openlicences.OpenLicense;
import no.fdk.referencedata.openlicences.OpenLicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OpenLicenseQuery implements GraphQLQueryResolver {

    private final OpenLicenseService openLicenseService;

    @Autowired
    public OpenLicenseQuery(OpenLicenseService openLicenseService) {
        this.openLicenseService = openLicenseService;
    }

    public List<OpenLicense> getOpenLicenses() {
        return openLicenseService.getAll().stream()
                .sorted(Comparator.comparing(OpenLicense::getUri))
                .collect(Collectors.toList());
    }

    public OpenLicense getOpenLicenseByCode(String code) {
        return openLicenseService.getByCode(code).orElse(null);
    }
}
