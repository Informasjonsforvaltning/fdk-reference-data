package no.fdk.referencedata.eu.datatheme;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/eu/data-themes")
@Slf4j
public class DataThemeController {

    @Autowired
    private DataThemeRepository dataThemeRepository;

    @Autowired
    private DataThemeService dataThemeService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<DataThemes> getDataThemes() {
        return ResponseEntity.ok(DataThemes.builder().dataThemes(
                StreamSupport.stream(dataThemeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(DataTheme::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getDataThemesRDF() {
        return ResponseEntity.ok(dataThemeService.getRdf(RDFFormat.TURTLE));
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateDataThemes() {
        dataThemeService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<DataTheme> getDataTheme(@PathVariable("code") String code) {
        return ResponseEntity.of(dataThemeRepository.findByCode(code));
    }
}
