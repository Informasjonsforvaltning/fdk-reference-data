package no.fdk.referencedata.linguisticsystem;

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
public class LinguisticSystemService {

    private List<LinguisticSystem> linguisticSystems = Collections.emptyList();

    public LinguisticSystemImporter linguisticSystemImporter;

    @Autowired
    public LinguisticSystemService(LinguisticSystemImporter linguisticSystemImporter) {
        this.linguisticSystemImporter = linguisticSystemImporter;
    }

    public List<LinguisticSystem> getAll() {
        return linguisticSystems;
    }

    public Optional<LinguisticSystem> getByCode(final String code) {
        return linguisticSystems.stream()
                .filter(s -> s.code.equals(code))
                .findFirst();
    }

    public String getRdf(RDFFormat rdfFormat) {
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, linguisticSystemImporter.getModel(), rdfFormat);
        return stringWriter.toString();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importLinguisticSystems() {
        log.debug("Importing linguistic systems");
        linguisticSystems = linguisticSystemImporter.importFromSource();
    }

}
