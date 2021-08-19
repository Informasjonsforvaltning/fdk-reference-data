package no.fdk.referencedata.eu.datatheme;

import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/eu/data-themes")
@Slf4j
public class DataThemeController {

    @Autowired
    private DataThemeRepository dataThemeRepository;

    @GetMapping
    public ResponseEntity<DataThemes> getDataThemes() {
        return ResponseEntity.ok(DataThemes.builder().dataThemes(
                StreamSupport.stream(dataThemeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(DataTheme::getUri))
                        .collect(Collectors.toList())).build());
    }

    @GetMapping(path = "/{code}")
    public ResponseEntity<DataTheme> getDataTheme(@PathVariable("code") String code) {
        return ResponseEntity.of(dataThemeRepository.findByCode(code));
    }
}
