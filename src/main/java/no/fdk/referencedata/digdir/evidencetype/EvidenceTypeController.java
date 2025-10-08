package no.fdk.referencedata.digdir.evidencetype;

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
@RequestMapping("/digdir/evidence-types")
@Slf4j
public class EvidenceTypeController {

    @Autowired
    private EvidenceTypeRepository evidenceTypeRepository;

    @Autowired
    private EvidenceTypeService evidenceTypeService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<EvidenceTypes> getEvidenceTypes() {
        return ResponseEntity.ok(EvidenceTypes.builder().evidenceTypes(
                StreamSupport.stream(evidenceTypeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(EvidenceType::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateEvidenceTypes() {
        evidenceTypeService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<EvidenceType> getEvidenceType(@PathVariable("code") String code) {
        return ResponseEntity.of(evidenceTypeRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getEvidenceTypesRDF() {
        return ResponseEntity.ok(evidenceTypeService.getRdf(RDFFormat.TURTLE));
    }
}
