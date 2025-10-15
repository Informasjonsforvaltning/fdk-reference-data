package no.fdk.referencedata.mobility.datastandard;

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
@RequestMapping("/mobility/data-standards")
@Slf4j
public class MobilityDataStandardController {
    private final MobilityDataStandardRepository mobilityDataStandardRepository;
    private final MobilityDataStandardService mobilityDataStandardService;

    @Autowired
    public MobilityDataStandardController(MobilityDataStandardRepository mobilityDataStandardRepository,
                                          MobilityDataStandardService mobilityDataStandardService) {
        this.mobilityDataStandardRepository = mobilityDataStandardRepository;
        this.mobilityDataStandardService = mobilityDataStandardService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<MobilityDataStandards> getMobilityDataStandards() {
        return ResponseEntity.ok(MobilityDataStandards.builder().mobilityDataStandards(
                StreamSupport.stream(mobilityDataStandardRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(MobilityDataStandard::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateMobilityDataStandards() {
        mobilityDataStandardService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<MobilityDataStandard> getRoleType(@PathVariable("code") String code) {
        return ResponseEntity.of(mobilityDataStandardRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getMobilityDataStandardsRDF() {
        return ResponseEntity.ok(mobilityDataStandardService.getRdf(RDFFormat.TURTLE));
    }
}
