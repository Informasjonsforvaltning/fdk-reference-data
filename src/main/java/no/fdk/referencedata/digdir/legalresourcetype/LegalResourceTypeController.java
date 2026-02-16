package no.fdk.referencedata.digdir.legalresourcetype;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/digdir/legal-resource-types")
@Slf4j
public class LegalResourceTypeController {

    @Autowired
    private LegalResourceTypeRepository legalResourceTypeRepository;

    @Autowired
    private LegalResourceTypeService legalResourceTypeService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<LegalResourceTypes> getLegalResourceTypes() {
        return ResponseEntity.ok(LegalResourceTypes.builder().legalResourceTypes(
                StreamSupport.stream(legalResourceTypeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(LegalResourceType::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateLegalResourceTypes() {
        legalResourceTypeService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<LegalResourceType> getLegalResourceType(@PathVariable("code") String code) {
        return ResponseEntity.of(legalResourceTypeRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getLegalResourceTypesRDF() {
        return ResponseEntity.ok(legalResourceTypeService.getRdf(RDFFormat.TURTLE));
    }
}
