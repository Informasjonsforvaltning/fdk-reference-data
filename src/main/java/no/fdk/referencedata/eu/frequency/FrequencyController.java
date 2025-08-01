package no.fdk.referencedata.eu.frequency;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/eu/frequencies")
@Slf4j
public class FrequencyController {

    @Autowired
    private FrequencyRepository frequencyRepository;

    @Autowired
    private FrequencyService frequencyService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<Frequencies> getFrequencies() {
        return ResponseEntity.ok(Frequencies.builder().frequencies(
                StreamSupport.stream(frequencyRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(Frequency::getSortIndex))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateFrequencies() {
        frequencyService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<Frequency> getFrequency(@PathVariable("code") String code) {
        return ResponseEntity.of(frequencyRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getFrequenciesRDF() {
        return ResponseEntity.ok(frequencyService.getRdf(RDFFormat.TURTLE));
    }
}
