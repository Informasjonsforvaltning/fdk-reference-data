package no.fdk.referencedata.adms.publishertype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adms/publisher-types")
@Slf4j
public class PublisherTypeController {

    private final PublisherTypeService publisherTypeService;

    @Autowired
    public PublisherTypeController(PublisherTypeService publisherTypeService) {
        this.publisherTypeService = publisherTypeService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<PublisherTypes> getPublisherTypees() {
        return ResponseEntity.ok(PublisherTypes.builder()
                .publisherTypes(publisherTypeService.getAll())
                .build());
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<PublisherType> getPublisherType(@PathVariable("code") final String code) {
        return ResponseEntity.of(publisherTypeService.getByCode(code));
    }

}
