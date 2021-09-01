package no.fdk.referencedata.eu.accessright;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/eu/access-rights")
@Slf4j
public class AccessRightController {

    @Autowired
    private AccessRightRepository accessRightRepository;

    @Autowired
    private AccessRightService accessRightService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<AccessRights> getDataThemes() {
        return ResponseEntity.ok(AccessRights.builder().accessRights(
                StreamSupport.stream(accessRightRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(AccessRight::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateAccessRights() {
        accessRightService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<AccessRight> getDataTheme(@PathVariable("code") String code) {
        return ResponseEntity.of(accessRightRepository.findByCode(code));
    }
}
