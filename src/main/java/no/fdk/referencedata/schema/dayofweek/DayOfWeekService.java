package no.fdk.referencedata.schema.dayofweek;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DayOfWeekService {

    private List<DayOfWeek> weekDays = Collections.emptyList();

    public DayOfWeekImporter dayOfWeekImporter;

    @Autowired
    public DayOfWeekService(DayOfWeekImporter dayOfWeekImporter) {
        this.dayOfWeekImporter = dayOfWeekImporter;
    }

    public List<DayOfWeek> getAll() {
        return weekDays;
    }

    public Optional<DayOfWeek> getByCode(final String code) {
        return weekDays.stream()
                .filter(s -> s.code.equals(code))
                .findFirst();
    }

    public String getRdf(RDFFormat rdfFormat) {
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, dayOfWeekImporter.getModel(), rdfFormat) ;
        return stringWriter.toString();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importWeekDays() {
        log.debug("Importing schema DayOfWeek");
        weekDays = dayOfWeekImporter.importFromSource();
    }

}
