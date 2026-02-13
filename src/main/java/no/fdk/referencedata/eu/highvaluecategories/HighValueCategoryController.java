package no.fdk.referencedata.eu.highvaluecategories;

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
@RequestMapping("/eu/high-value-categories")
@Slf4j
public class HighValueCategoryController {

    @Autowired
    private HighValueCategoryRepository highValueCategoryRepository;

    @Autowired
    private HighValueCategoryService highValueCategoryService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<HighValueCategories> getHighValueCategories() {
        return ResponseEntity.ok(HighValueCategories.builder().highValueCategories(
                StreamSupport.stream(highValueCategoryRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(HighValueCategory::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateHighValueCategories() {
        highValueCategoryService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<HighValueCategory> getHighValueCategory(@PathVariable("code") String code) {
        return ResponseEntity.of(highValueCategoryRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getHighValueCategoriesRDF() {
        return ResponseEntity.ok(highValueCategoryService.getRdf(RDFFormat.TURTLE));
    }
}
