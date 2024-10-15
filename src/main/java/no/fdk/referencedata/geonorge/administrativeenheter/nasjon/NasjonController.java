package no.fdk.referencedata.geonorge.administrativeenheter.nasjon;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/geonorge/administrative-enheter/nasjoner")
@Slf4j
public class NasjonController {

    @Autowired
    private NasjonService nasjonService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<Nasjoner> getNasjoner() {
        return ResponseEntity.ok(Nasjoner.builder().nasjoner(nasjonService.getNasjoner()).build());
    }

    @CrossOrigin
    @GetMapping(path = "/{nasjonsnummer}")
    public ResponseEntity<Nasjon> getNasjon(@PathVariable("nasjonsnummer") String nasjonsnummer) {
        return ResponseEntity.of(nasjonService.getNasjonByNasjonsnummer(nasjonsnummer));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getNasjonerRDF() {
        return ResponseEntity.ok(nasjonService.getRdf(RDFFormat.TURTLE));
    }
}
