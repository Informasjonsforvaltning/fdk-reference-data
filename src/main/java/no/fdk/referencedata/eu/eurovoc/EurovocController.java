package no.fdk.referencedata.eu.eurovoc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eu/eurovoc")
@Slf4j
public class EurovocController {

    @Autowired
    private EurovocRepository eurovocRepository;

    @GetMapping(path = "/{code}")
    public ResponseEntity<Eurovoc> getEurovoc(@PathVariable("code") String code) {
        return ResponseEntity.of(eurovocRepository.findByCode(code));
    }
}
