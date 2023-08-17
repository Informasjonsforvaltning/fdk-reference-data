package no.fdk.referencedata.digdir.conceptsubjects;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/digdir/concept-subjects")
@Slf4j
public class ConceptSubjectController {

    @Autowired
    private ConceptSubjectRepository conceptSubjectRepository;

    @Autowired
    private ConceptSubjectService conceptSubjectService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<ConceptSubjects> getConceptSubjects() {
        return ResponseEntity.ok(ConceptSubjects.builder().conceptSubjects(
                StreamSupport.stream(conceptSubjectRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(ConceptSubject::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateConceptSubjects() {
        conceptSubjectService.harvestAndSave();
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getConceptSubjectsRDF() {
        return ResponseEntity.ok(conceptSubjectService.getRdf(RDFFormat.TURTLE));
    }
}
