package no.fdk.referencedata.mobility.theme;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/mobility/themes")
@Slf4j
public class MobilityThemeController {
    private final MobilityThemeRepository mobilityThemeRepository;
    private final MobilityThemeService mobilityThemeService;

    @Autowired
    public MobilityThemeController(MobilityThemeRepository mobilityThemeRepository,
                                   MobilityThemeService mobilityThemeService) {
        this.mobilityThemeRepository = mobilityThemeRepository;
        this.mobilityThemeService = mobilityThemeService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<MobilityThemes> getMobilityThemes() {
        return ResponseEntity.ok(MobilityThemes.builder().mobilityThemes(
                StreamSupport.stream(mobilityThemeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(MobilityTheme::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateMobilityThemes() {
        mobilityThemeService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<MobilityTheme> getRoleType(@PathVariable("code") String code) {
        return ResponseEntity.of(mobilityThemeRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getMobilityThemesRDF() {
        return ResponseEntity.ok(mobilityThemeService.getRdf(RDFFormat.TURTLE));
    }
}
