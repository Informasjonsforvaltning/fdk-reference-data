package no.fdk.referencedata.eu.country;

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
@RequestMapping("/eu/countries")
@Slf4j
public class CountryController {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CountryService countryService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<Countries> getCountries() {
        return ResponseEntity.ok(Countries.builder().countries(
                StreamSupport.stream(countryRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(Country::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateCountries() {
        countryService.harvestAndSave();
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<Country> getFileType(@PathVariable("code") String code) {
        return ResponseEntity.of(countryRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getCountriesRDF() {
        return ResponseEntity.ok(countryService.getRdf(RDFFormat.TURTLE));
    }
}
