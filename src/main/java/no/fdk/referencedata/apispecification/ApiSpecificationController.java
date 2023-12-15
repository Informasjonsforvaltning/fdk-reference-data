package no.fdk.referencedata.apispecification;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api-specifications")
@Slf4j
public class ApiSpecificationController {

    private ApiSpecificationService apiSpecificationService;

    @Autowired
    public ApiSpecificationController(ApiSpecificationService apiSpecificationService) {
        this.apiSpecificationService = apiSpecificationService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<ApiSpecifications> getApiSpecifications() {
        return ResponseEntity.ok(ApiSpecifications.builder()
                .apiSpecifications(apiSpecificationService.getAll())
                .build());
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<ApiSpecification> getApiSpecification(@PathVariable("code") final String code) {
        return ResponseEntity.of(apiSpecificationService.getByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public String getApiSpecificationsRDF() {
        return apiSpecificationService.getRdf(RDFFormat.TURTLE);
    }

}
