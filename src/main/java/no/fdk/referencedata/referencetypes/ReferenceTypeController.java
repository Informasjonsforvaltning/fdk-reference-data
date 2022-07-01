package no.fdk.referencedata.referencetypes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reference-types")
@Slf4j
public class ReferenceTypeController {

    private ReferenceTypeService referenceTypeService;

    @Autowired
    public ReferenceTypeController(ReferenceTypeService referenceTypeService) {
        this.referenceTypeService = referenceTypeService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<ReferenceTypes> getReferenceTypes() {
        return ResponseEntity.ok(ReferenceTypes.builder()
                .referenceTypes(referenceTypeService.getAll())
                .build());
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<ReferenceType> getReferenceType(@PathVariable("code") final String code) {
        return ResponseEntity.of(referenceTypeService.getByCode(code));
    }

}
