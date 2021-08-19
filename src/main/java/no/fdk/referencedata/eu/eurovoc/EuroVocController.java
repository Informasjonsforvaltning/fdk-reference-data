package no.fdk.referencedata.eu.eurovoc;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.datatheme.DataTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/eu/eurovocs")
@Slf4j
public class EuroVocController {

    @Autowired
    private EuroVocRepository euroVocRepository;

    @GetMapping
    public ResponseEntity<EuroVocs> getEuroVocs() {
        return ResponseEntity.ok(EuroVocs.builder().euroVocs(
                StreamSupport.stream(euroVocRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(EuroVoc::getUri))
                        .collect(Collectors.toList())).build());
    }

    @GetMapping(path = "/{code}")
    public ResponseEntity<EuroVoc> getEuroVoc(@PathVariable("code") String code) {
        return ResponseEntity.of(euroVocRepository.findByCode(code));
    }
}
