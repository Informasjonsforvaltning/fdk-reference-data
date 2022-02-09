package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/geonorge/administrative-enheter/kommuner")
@Slf4j
public class KommuneController {

    @Autowired
    private KommuneRepository kommuneRepository;

    @Autowired
    private KommuneService kommuneService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<Kommuner> getKommuner() {
        return ResponseEntity.ok(Kommuner.builder().kommuner(
                StreamSupport.stream(kommuneRepository.findAll().spliterator(), false)
                    .sorted(Comparator.comparing(Kommune::getUri))
                    .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateKommuner() {
        kommuneService.harvestAndSave();
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{kommunenummer}")
    public ResponseEntity<Kommune> getKommune(@PathVariable("kommunenummer") final String kommunenummer) {
        return ResponseEntity.of(kommuneRepository.findByKommunenummer(kommunenummer));
    }
}
