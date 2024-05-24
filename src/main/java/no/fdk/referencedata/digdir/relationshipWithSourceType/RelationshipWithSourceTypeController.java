package no.fdk.referencedata.digdir.relationshipWithSourceType;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/digdir/relationship-with-source-types")
@Slf4j
public class RelationshipWithSourceTypeTypeController {

    @Autowired
    private RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;

    @Autowired
    private RelationshipWithSourceTypeService relationshipWithSourceTypeService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<RelationshipWithSourceTypes> getRelationshipWithSourceTypes() {
        return ResponseEntity.ok(RelationshipWithSourceTypes.builder().relationshipWithSourceTypes(
                StreamSupport.stream(relationshipWithSourceTypeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(RelationshipWithSourceType::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateRelationshipWithSourceTypes() {
        relationshipWithSourceTypeService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<RelationshipWithSourceType> getRelationshipWithSourceType(@PathVariable("code") String code) {
        return ResponseEntity.of(relationshipWithSourceTypeRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getRelationshipWithSourceTypesRDF() {
        return ResponseEntity.ok(relationshipWithSourceTypeService.getRdf(RDFFormat.TURTLE));
    }
}
