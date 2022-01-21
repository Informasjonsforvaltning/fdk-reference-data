package no.fdk.referencedata.eu.distributiontype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/eu/distribution-types")
@Slf4j
public class DistributionTypeController {

    @Autowired
    private DistributionTypeRepository distributionTypeRepository;

    @Autowired
    private DistributionTypeService distributionTypeService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<DistributionTypes> getDistributionTypes() {
        return ResponseEntity.ok(DistributionTypes.builder().distributionTypes(
                StreamSupport.stream(distributionTypeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(DistributionType::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateDistributionTypes() {
        distributionTypeService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<DistributionType> getDistributionType(@PathVariable("code") String code) {
        return ResponseEntity.of(distributionTypeRepository.findByCode(code));
    }
}
