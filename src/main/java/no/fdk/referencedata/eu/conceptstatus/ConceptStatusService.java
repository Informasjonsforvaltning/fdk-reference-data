package no.fdk.referencedata.eu.conceptstatus;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConceptStatusService {
    private final String dbSourceID = "concept-status-source";

    private final ConceptStatusHarvester conceptStatusHarvester;

    private final ConceptStatusWriter conceptStatusWriter;
    private final ConceptStatusRepository conceptStatusRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ConceptStatusService(
            ConceptStatusHarvester conceptStatusHarvester,
            ConceptStatusRepository conceptStatusRepository,
            RDFSourceRepository rdfSourceRepository,
            ConceptStatusWriter conceptStatusWriter) {
        this.conceptStatusHarvester = conceptStatusHarvester;
        this.conceptStatusRepository = conceptStatusRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.conceptStatusWriter = conceptStatusWriter;
    }

    public boolean firstTime() {
        return conceptStatusRepository.count() == 0;
    }

    public Optional<ConceptStatus> getConceptStatus(String code) {
        return conceptStatusRepository.findByCode(code);
    }

    public ConceptStatuses getConceptStatuses() {
        return ConceptStatuses.builder().conceptStatuses(
                conceptStatusRepository.findAll().stream()
                        .sorted(Comparator.comparing(ConceptStatus::getUri))
                        .collect(Collectors.toList())).build();
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(dbSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    public void harvestAndSave() {
        try {

            final List<ConceptStatus> items = new ArrayList<>();
            conceptStatusHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} concept status", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(conceptStatusHarvester.getModel(), RDFFormat.TURTLE));


            conceptStatusWriter.replaceAll(items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest concept statuses", e);
        }
    }
}
