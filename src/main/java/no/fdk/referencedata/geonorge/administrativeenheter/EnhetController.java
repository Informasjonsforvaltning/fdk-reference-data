package no.fdk.referencedata.geonorge.administrativeenheter;

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
@RequestMapping("/geonorge/administrative-enheter")
@Slf4j
public class EnhetController {

    private final EnhetRepository enhetRepository;

    private final EnhetService enhetService;

    @Autowired
    public EnhetController(EnhetRepository enhetRepository, EnhetService enhetService) {
        this.enhetRepository = enhetRepository;
        this.enhetService = enhetService;
    }


    @CrossOrigin
    @GetMapping
    public ResponseEntity<Enheter> findAll() {
        return ResponseEntity.ok(Enheter.builder().enheter(
                StreamSupport.stream(enhetRepository.findAll().spliterator(), false)
                    .sorted(Comparator.comparing(Enhet::getUri))
                    .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateEnheter() {
        enhetService.harvestAndSave();
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<Enhet> findByCode(@PathVariable("code") final String code) {
        return ResponseEntity.of(enhetRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getRDF() {
        return ResponseEntity.ok(enhetService.getRdf(RDFFormat.TURTLE));
    }
}
