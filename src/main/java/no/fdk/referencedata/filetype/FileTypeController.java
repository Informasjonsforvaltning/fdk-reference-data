package no.fdk.referencedata.filetype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("file-types")
@Slf4j
public class FileTypeController {

    @Autowired
    private FileTypeRepository fileTypeRepository;

    @GetMapping
    public ResponseEntity<FileTypes> getFileTypes() {
        return ResponseEntity.ok(FileTypes.builder().fileTypes(
                StreamSupport.stream(fileTypeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(FileType::getUri))
                        .collect(Collectors.toList())).build());
    }

    @GetMapping(path = "/{uri}")
    public ResponseEntity<FileType> getFileType(@PathVariable("uri") String uri) {
        final String decodedUri = URLDecoder.decode(uri, StandardCharsets.UTF_8);
        log.debug("Find fileType for uri: " + decodedUri);
        return ResponseEntity.of(fileTypeRepository.findById(decodedUri));
    }
}
