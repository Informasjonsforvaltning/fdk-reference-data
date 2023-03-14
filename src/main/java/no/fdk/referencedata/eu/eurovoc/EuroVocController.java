package no.fdk.referencedata.eu.eurovoc;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/eu/eurovocs")
@Slf4j
public class EuroVocController {

    @Autowired
    private EuroVocRepository euroVocRepository;

    @Autowired
    private EuroVocService euroVocService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<EuroVocs> getEuroVocs() {
        return ResponseEntity.ok(EuroVocs.builder().euroVocs(
                StreamSupport.stream(euroVocRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(EuroVoc::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getEuroVocsRDF() {
        return ResponseEntity.ok(euroVocService.getRdf(RDFFormat.TURTLE));
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateEuroVocs() {
        euroVocService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<EuroVoc> getEuroVoc(@PathVariable("code") String code) {
        return ResponseEntity.of(euroVocRepository.findByCode(code));
    }
}
