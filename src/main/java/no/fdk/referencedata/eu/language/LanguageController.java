package no.fdk.referencedata.eu.language;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/eu/languages")
@Slf4j
public class LanguageController {

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private LanguageService languageService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<Languages> getLanguages() {
        return ResponseEntity.ok(Languages.builder().languages(
                languageRepository.findAll().stream()
                        .sorted(Comparator.comparing(Language::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateLanguages() {
        languageService.harvestAndSave();
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<Language> getLanguage(@PathVariable("code") String code) {
        return ResponseEntity.of(languageRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getLanguagesRDF() {
        return ResponseEntity.ok(languageService.getRdf(RDFFormat.TURTLE));
    }
}
