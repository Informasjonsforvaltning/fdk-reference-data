package no.fdk.referencedata.eu.continent;

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
@RequestMapping("/eu/continents")
@Slf4j
public class ContinentController {

    @Autowired
    private ContinentRepository continentRepository;

    @Autowired
    private ContinentService continentService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<Continents> getContinents() {
        return ResponseEntity.ok(Continents.builder().continents(
                StreamSupport.stream(continentRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(Continent::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateContinents() {
        continentService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<Continent> getContinent(@PathVariable("code") String code) {
        return ResponseEntity.of(continentRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getContinentsRDF() {
        return ResponseEntity.ok(continentService.getRdf(RDFFormat.TURTLE));
    }
}
