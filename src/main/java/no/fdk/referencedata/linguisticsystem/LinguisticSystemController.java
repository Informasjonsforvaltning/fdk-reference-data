package no.fdk.referencedata.linguisticsystem;

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
@RequestMapping("/linguistic-systems")
@Slf4j
public class LinguisticSystemController {

    private LinguisticSystemService linguisticSystemService;

    @Autowired
    public LinguisticSystemController(LinguisticSystemService linguisticSystemService) {
        this.linguisticSystemService = linguisticSystemService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<LinguisticSystems> getLinguisticSystems() {
        return ResponseEntity.ok(LinguisticSystems.builder()
                .linguisticSystems(linguisticSystemService.getAll())
                .build());
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<LinguisticSystem> getLinguisticSystem(@PathVariable("code") final String code) {
        return ResponseEntity.of(linguisticSystemService.getByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public String getLinguisticSystemsRDF() {
        return linguisticSystemService.getRdf(RDFFormat.TURTLE);
    }

}
