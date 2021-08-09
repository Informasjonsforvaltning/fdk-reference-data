package no.fdk.referencedata.mediatype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("media-types")
@Slf4j
public class MediaTypeController {

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    @GetMapping
    public ResponseEntity<MediaTypes> getMediaTypes() {
        return ResponseEntity.ok(MediaTypes.builder().mediaTypes(
                StreamSupport.stream(mediaTypeRepository.findAll().spliterator(), false)
                    .sorted(Comparator.comparing(MediaType::getUri))
                    .collect(Collectors.toList())).build());
    }

    @GetMapping(path = "/{type}")
    public ResponseEntity<MediaTypes> getMediaType(@PathVariable("type") final String type) {
        return ResponseEntity.ok(MediaTypes.builder().mediaTypes(
                mediaTypeRepository.findByType(type).stream()
                    .sorted(Comparator.comparing(MediaType::getUri))
                    .collect(Collectors.toList())).build());
}

    @GetMapping(path = "/{type}/{subType}")
    public ResponseEntity<MediaType> getMediaType(@PathVariable("type") final String type,
                                                  @PathVariable("subType") final String subType) {
        return ResponseEntity.of(mediaTypeRepository.findByTypeAndSubType(type, subType));
    }
}
