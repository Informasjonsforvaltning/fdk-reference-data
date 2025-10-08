package no.fdk.referencedata.eu.conceptstatus;

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

@RestController
@RequestMapping("/eu/concept-statuses")
@Slf4j
public class ConceptStatusController {

    @Autowired
    private ConceptStatusService conceptStatusService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<ConceptStatuses> getConceptStatuses() {
        return ResponseEntity.ok(conceptStatusService.getConceptStatuses());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateConceptStatuses() {
        conceptStatusService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getConceptStatusesRDF() {
        return ResponseEntity.ok(conceptStatusService.getRdf(RDFFormat.TURTLE));
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<ConceptStatus> getConceptStatus(@PathVariable("code") String code) {
        return ResponseEntity.of(conceptStatusService.getConceptStatus(code));
    }
}
