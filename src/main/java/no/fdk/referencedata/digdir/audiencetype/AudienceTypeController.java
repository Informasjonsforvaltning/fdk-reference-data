package no.fdk.referencedata.digdir.audiencetype;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/digdir/audience-types")
@Slf4j
public class AudienceTypeController {

    @Autowired
    private AudienceTypeRepository audienceTypeRepository;

    @Autowired
    private AudienceTypeService audienceTypeService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<AudienceTypes> getAudienceTypes() {
        return ResponseEntity.ok(AudienceTypes.builder().audienceTypes(
                StreamSupport.stream(audienceTypeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(AudienceType::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateAudienceTypes() {
        audienceTypeService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<AudienceType> getAudienceType(@PathVariable("code") String code) {
        return ResponseEntity.of(audienceTypeRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getAudienceTypesRDF() {
        return ResponseEntity.ok(audienceTypeService.getRdf(RDFFormat.TURTLE));
    }
}
