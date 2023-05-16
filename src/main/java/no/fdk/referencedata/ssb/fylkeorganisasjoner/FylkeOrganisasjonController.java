package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/ssb/fylke-organisasjoner")
@Slf4j
public class FylkeOrganisasjonController {

    private final FylkeOrganisasjonRepository fylkeOrganisasjonRepository;
    private final FylkeOrganisasjonService fylkeOrganisasjonService;

    @Autowired
    public FylkeOrganisasjonController(FylkeOrganisasjonRepository fylkeOrganisasjonRepository,
                                       FylkeOrganisasjonService fylkeOrganisasjonService) {
        this.fylkeOrganisasjonRepository = fylkeOrganisasjonRepository;
        this.fylkeOrganisasjonService = fylkeOrganisasjonService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<FylkeOrganisasjoner> getFylkeOrganisasjoner() {
        return ResponseEntity.ok(FylkeOrganisasjoner.builder().fylkeOrganisasjoner(
                StreamSupport.stream(fylkeOrganisasjonRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(FylkeOrganisasjon::getOrganisasjonsnummer))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateFylkeOrganisasjoner() {
        fylkeOrganisasjonService.harvestAndSave();
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{fylkesnummer}")
    public ResponseEntity<FylkeOrganisasjon> getFylkeOrganisasjon(@PathVariable("fylkesnummer") String fylkesnummer) {
        return ResponseEntity.of(fylkeOrganisasjonRepository.findByFylkesnummer(fylkesnummer));
    }
}
