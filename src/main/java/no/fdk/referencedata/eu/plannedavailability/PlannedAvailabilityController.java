package no.fdk.referencedata.eu.plannedavailability;

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
@RequestMapping("/eu/planned-availabilities")
@Slf4j
public class PlannedAvailabilityController {

    @Autowired
    private PlannedAvailabilityRepository plannedAvailabilityRepository;

    @Autowired
    private PlannedAvailabilityService plannedAvailabilityService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<PlannedAvailabilities> getPlannedAvailabilities() {
        return ResponseEntity.ok(PlannedAvailabilities.builder().plannedAvailabilities(
                StreamSupport.stream(plannedAvailabilityRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(PlannedAvailability::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updatePlannedAvailabilities() {
        plannedAvailabilityService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<PlannedAvailability> getPlannedAvailability(@PathVariable("code") String code) {
        return ResponseEntity.of(plannedAvailabilityRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getPlannedAvailabilitiesRDF() {
        return ResponseEntity.ok(plannedAvailabilityService.getRdf(RDFFormat.TURTLE));
    }
}
