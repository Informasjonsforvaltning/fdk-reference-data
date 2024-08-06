package no.fdk.referencedata.eu.datasettype;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/eu/dataset-types")
@Slf4j
public class DatasetTypeController {

    @Autowired
    private DatasetTypeRepository datasetTypeRepository;

    @Autowired
    private DatasetTypeService datasetTypeService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<DatasetTypes> getDatasetTypes() {
        return ResponseEntity.ok(DatasetTypes.builder().datasetTypes(
                StreamSupport.stream(datasetTypeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(DatasetType::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateDatasetTypes() {
        datasetTypeService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<DatasetType> getDatasetType(@PathVariable("code") String code) {
        return ResponseEntity.of(datasetTypeRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getDatasetTypesRDF() {
        return ResponseEntity.ok(datasetTypeService.getRdf(RDFFormat.TURTLE));
    }
}
