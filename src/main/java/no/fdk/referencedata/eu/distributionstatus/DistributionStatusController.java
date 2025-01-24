package no.fdk.referencedata.eu.distributionstatus;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/eu/distribution-statuses")
@Slf4j
public class DistributionStatusController {

    @Autowired
    private DistributionStatusRepository distributionStatusRepository;

    @Autowired
    private DistributionStatusService distributionStatusService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<DistributionStatuses> getDistributionStatuses() {
        return ResponseEntity.ok(DistributionStatuses.builder().distributionStatuses(
                StreamSupport.stream(distributionStatusRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(DistributionStatus::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateDistributionStatuses() {
        distributionStatusService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<DistributionStatus> getDistributionStatus(@PathVariable("code") String code) {
        return ResponseEntity.of(distributionStatusRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getDistributionStatusesRDF() {
        return ResponseEntity.ok(distributionStatusService.getRdf(RDFFormat.TURTLE));
    }
}
