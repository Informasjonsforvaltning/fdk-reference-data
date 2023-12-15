package no.fdk.referencedata.provenancestatement;

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
@RequestMapping("/provenance-statements")
@Slf4j
public class ProvenanceStatementController {

    private ProvenanceStatementService provenanceStatementService;

    @Autowired
    public ProvenanceStatementController(ProvenanceStatementService provenanceStatementService) {
        this.provenanceStatementService = provenanceStatementService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<ProvenanceStatements> getProvenanceStatements() {
        return ResponseEntity.ok(ProvenanceStatements.builder()
                .provenanceStatements(provenanceStatementService.getAll())
                .build());
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<ProvenanceStatement> getProvenanceStatement(@PathVariable("code") final String code) {
        return ResponseEntity.of(provenanceStatementService.getByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public String getProvenanceStatementsRDF() {
        return provenanceStatementService.getRdf(RDFFormat.TURTLE);
    }

}
