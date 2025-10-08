package no.fdk.referencedata.eu.licence;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/eu/licences")
@Slf4j
public class LicenceController {

    @Autowired
    private LicenceRepository licenceRepository;

    @Autowired
    private LicenceService licenceService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<Licences> getLicences() {
        return ResponseEntity.ok(Licences.builder().licences(
                StreamSupport.stream(licenceRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(Licence::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateLicences() {
        licenceService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<Licence> getLicence(@PathVariable("code") String code) {
        return ResponseEntity.of(licenceRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getLicencesRDF() {
        return ResponseEntity.ok(licenceService.getRdf(RDFFormat.TURTLE));
    }
}
