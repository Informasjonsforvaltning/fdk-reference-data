package no.fdk.referencedata.eu.filetype;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.eurovoc.EuroVocService;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/eu/file-types")
@Slf4j
public class FileTypeController {

    @Autowired
    private FileTypeRepository fileTypeRepository;

    @Autowired
    private FileTypeService fileTypeService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<FileTypes> getFileTypes() {
        return ResponseEntity.ok(FileTypes.builder().fileTypes(
                StreamSupport.stream(fileTypeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(FileType::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @SecurityRequirement(name = "apiKey")
    @PostMapping
    public ResponseEntity<Void> updateFileTypes() {
        fileTypeService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<FileType> getFileType(@PathVariable("code") String code) {
        return ResponseEntity.of(fileTypeRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getFileTypesRDF() {
        return ResponseEntity.ok(fileTypeService.getRdf(RDFFormat.TURTLE));
    }
}
