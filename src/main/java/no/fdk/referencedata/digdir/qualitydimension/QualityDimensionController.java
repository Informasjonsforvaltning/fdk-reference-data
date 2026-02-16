package no.fdk.referencedata.digdir.qualitydimension;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/digdir/quality-dimensions")
@Slf4j
public class QualityDimensionController {

    @Autowired
    private QualityDimensionRepository qualityDimensionRepository;

    @Autowired
    private QualityDimensionService qualityDimensionService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<QualityDimensions> getQualityDimensions() {
        return ResponseEntity.ok(QualityDimensions.builder().qualityDimensions(
                StreamSupport.stream(qualityDimensionRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(QualityDimension::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateQualityDimensions() {
        qualityDimensionService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<QualityDimension> getQualityDimension(@PathVariable("code") String code) {
        return ResponseEntity.of(qualityDimensionRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getQualityDimensionsRDF() {
        return ResponseEntity.ok(qualityDimensionService.getRdf(RDFFormat.TURTLE));
    }
}
