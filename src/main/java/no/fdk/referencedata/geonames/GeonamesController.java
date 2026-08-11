package no.fdk.referencedata.geonames;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/geonames")
@Slf4j
public class GeonamesController {

    private final GeonamesFylkeRepository geonamesFylkeRepository;
    private final GeonamesKommuneRepository geonamesKommuneRepository;
    private final GeonamesService geonamesService;

    @Autowired
    public GeonamesController(GeonamesFylkeRepository geonamesFylkeRepository,
                               GeonamesKommuneRepository geonamesKommuneRepository,
                               GeonamesService geonamesService) {
        this.geonamesFylkeRepository = geonamesFylkeRepository;
        this.geonamesKommuneRepository = geonamesKommuneRepository;
        this.geonamesService = geonamesService;
    }

    @CrossOrigin
    @GetMapping("/fylker")
    public ResponseEntity<GeonamesFylker> getFylker() {
        return ResponseEntity.ok(GeonamesFylker.builder().fylker(
                StreamSupport.stream(geonamesFylkeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(GeonamesFylke::getName))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @GetMapping("/fylker/{geonameId}")
    public ResponseEntity<GeonamesFylke> getFylke(@PathVariable("geonameId") String geonameId) {
        return ResponseEntity.of(geonamesFylkeRepository.findByGeonameId(geonameId));
    }

    @CrossOrigin
    @GetMapping("/kommuner")
    public ResponseEntity<GeonamesKommuner> getKommuner() {
        return ResponseEntity.ok(GeonamesKommuner.builder().kommuner(
                StreamSupport.stream(geonamesKommuneRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(GeonamesKommune::getName))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @GetMapping("/kommuner/{geonameId}")
    public ResponseEntity<GeonamesKommune> getKommune(@PathVariable("geonameId") String geonameId) {
        return ResponseEntity.of(geonamesKommuneRepository.findByGeonameId(geonameId));
    }

    @CrossOrigin
    @GetMapping("/fylker/{geonameId}/kommuner")
    public ResponseEntity<GeonamesKommuner> getKommunerForFylke(@PathVariable("geonameId") String geonameId) {
        return ResponseEntity.ok(GeonamesKommuner.builder().kommuner(
                geonamesKommuneRepository.findByFylkeGeonameId(geonameId)
                        .stream()
                        .sorted(Comparator.comparing(GeonamesKommune::getName))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping("/fylker")
    public ResponseEntity<Void> updateGeonames() {
        return geonamesService.harvestAndSave().isSuccess()
                ? ResponseEntity.ok().build()
                : ResponseEntity.internalServerError().build();
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getRDF() {
        return ResponseEntity.ok(geonamesService.getRdf(RDFFormat.TURTLE));
    }
}
