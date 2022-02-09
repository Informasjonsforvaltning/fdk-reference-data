package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/geonorge/administrative-enheter/fylker")
@Slf4j
public class FylkeController {

    @Autowired
    private FylkeRepository fylkeRepository;

    @Autowired
    private FylkeService fylkeService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<Fylker> getFylker() {
        return ResponseEntity.ok(Fylker.builder().fylker(
                StreamSupport.stream(fylkeRepository.findAll().spliterator(), false)
                    .sorted(Comparator.comparing(Fylke::getUri))
                    .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateFylker() {
        fylkeService.harvestAndSave();
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{fylkesnummer}")
    public ResponseEntity<Fylke> getKommune(@PathVariable("fylkesnummer") final String fylkesnummer) {
        return ResponseEntity.of(fylkeRepository.findByFylkesnummer(fylkesnummer));
    }
}
