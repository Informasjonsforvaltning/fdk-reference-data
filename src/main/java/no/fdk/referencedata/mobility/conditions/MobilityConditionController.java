package no.fdk.referencedata.mobility.conditions;

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
@RequestMapping("/mobility/conditions-for-access-and-usage")
@Slf4j
public class MobilityConditionController {
    private final MobilityConditionRepository mobilityConditionRepository;
    private final MobilityConditionService mobilityConditionService;

    @Autowired
    public MobilityConditionController(MobilityConditionRepository mobilityConditionRepository,
                                       MobilityConditionService mobilityConditionService) {
        this.mobilityConditionRepository = mobilityConditionRepository;
        this.mobilityConditionService = mobilityConditionService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<MobilityConditions> getMobilityConditions() {
        return ResponseEntity.ok(MobilityConditions.builder().mobilityConditions(
                StreamSupport.stream(mobilityConditionRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(MobilityCondition::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateMobilityConditions() {
        mobilityConditionService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<MobilityCondition> getRoleType(@PathVariable("code") String code) {
        return ResponseEntity.of(mobilityConditionRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getMobilityConditionsRDF() {
        return ResponseEntity.ok(mobilityConditionService.getRdf(RDFFormat.TURTLE));
    }
}
