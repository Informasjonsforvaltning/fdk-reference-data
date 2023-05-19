package no.fdk.referencedata.ssb.kommuneorganisasjoner;

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
@RequestMapping("/ssb/kommune-organisasjoner")
@Slf4j
public class KommuneOrganisasjonController {

    private final KommuneOrganisasjonRepository kommuneOrganisasjonRepository;
    private final KommuneOrganisasjonService kommuneOrganisasjonService;

    @Autowired
    public KommuneOrganisasjonController(KommuneOrganisasjonRepository kommuneOrganisasjonRepository,
                                         KommuneOrganisasjonService kommuneOrganisasjonService) {
        this.kommuneOrganisasjonRepository = kommuneOrganisasjonRepository;
        this.kommuneOrganisasjonService = kommuneOrganisasjonService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<KommuneOrganisasjoner> getKommuneOrganisasjoner() {
        return ResponseEntity.ok(KommuneOrganisasjoner.builder().kommuneOrganisasjoner(
                StreamSupport.stream(kommuneOrganisasjonRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(KommuneOrganisasjon::getOrganisasjonsnummer))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateKommuneOrganisasjoner() {
        kommuneOrganisasjonService.harvestAndSave();
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{kommunenummer}")
    public ResponseEntity<KommuneOrganisasjon> getKommuneOrganisasjon(@PathVariable("kommunenummer") String kommunenummer) {
        return ResponseEntity.of(kommuneOrganisasjonRepository.findByKommunenummer(kommunenummer));
    }
}
