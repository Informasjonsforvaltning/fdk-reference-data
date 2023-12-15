package no.fdk.referencedata.openlicences;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/open-licenses")
@Slf4j
public class OpenLicenseController {

    private OpenLicenseService openLicenseService;

    @Autowired
    public OpenLicenseController(OpenLicenseService openLicenseService) {
        this.openLicenseService = openLicenseService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<OpenLicenses> getOpenLicenses() {
        return ResponseEntity.ok(OpenLicenses.builder()
                .openLicenses(openLicenseService.getAll())
                .build());
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<OpenLicense> getOpenLicense(@PathVariable("code") final String code) {
        return ResponseEntity.of(openLicenseService.getByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public String getOpenLicensesRDF() {
        return openLicenseService.getRdf(RDFFormat.TURTLE);
    }

}
