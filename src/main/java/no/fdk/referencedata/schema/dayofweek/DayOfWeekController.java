package no.fdk.referencedata.schema.dayofweek;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schema/week-days")
@Slf4j
public class DayOfWeekController {

    private final DayOfWeekService dayOfWeekService;

    @Autowired
    public DayOfWeekController(DayOfWeekService dayOfWeekService) {
        this.dayOfWeekService = dayOfWeekService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<WeekDays> getWeekDays() {
        return ResponseEntity.ok(WeekDays.builder()
                .weekDays(dayOfWeekService.getAll())
                .build());
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<DayOfWeek> getDayOfWeek(@PathVariable("code") final String code) {
        return ResponseEntity.of(dayOfWeekService.getByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public String getWeekDaysRDF() {
        return dayOfWeekService.getRdf(RDFFormat.TURTLE);
    }

}
