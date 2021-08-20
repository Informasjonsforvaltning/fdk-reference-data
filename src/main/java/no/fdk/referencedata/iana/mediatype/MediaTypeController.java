package no.fdk.referencedata.iana.mediatype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/iana/media-types")
@Slf4j
public class MediaTypeController {

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<MediaTypes> getMediaTypes() {
        return ResponseEntity.ok(MediaTypes.builder().mediaTypes(
                StreamSupport.stream(mediaTypeRepository.findAll().spliterator(), false)
                    .sorted(Comparator.comparing(MediaType::getUri))
                    .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @GetMapping(path = "/{type}")
    public ResponseEntity<MediaTypes> getMediaType(@PathVariable("type") final String type) {
        return ResponseEntity.ok(MediaTypes.builder().mediaTypes(
                mediaTypeRepository.findByType(type).stream()
                    .sorted(Comparator.comparing(MediaType::getUri))
                    .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @GetMapping(path = "/{type}/{subType}")
    public ResponseEntity<MediaType> getMediaType(@PathVariable("type") final String type,
                                                  @PathVariable("subType") final String subType) {
        return ResponseEntity.of(mediaTypeRepository.findByTypeAndSubType(type, subType));
    }
}
