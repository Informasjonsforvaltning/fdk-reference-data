package no.fdk.referencedata.eu.mainactivity;

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
@RequestMapping("/eu/main-activities")
@Slf4j
public class MainActivityController {

    @Autowired
    private MainActivityRepository mainActivityRepository;

    @Autowired
    private MainActivityService mainActivityService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<MainActivities> getMainActivities() {
        return ResponseEntity.ok(MainActivities.builder().mainActivities(
                StreamSupport.stream(mainActivityRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(MainActivity::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateMainActivities() {
        mainActivityService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<MainActivity> getMainActivity(@PathVariable("code") String code) {
        return ResponseEntity.of(mainActivityRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getMainActivitiesRDF() {
        return ResponseEntity.ok(mainActivityService.getRdf(RDFFormat.TURTLE));
    }
}
