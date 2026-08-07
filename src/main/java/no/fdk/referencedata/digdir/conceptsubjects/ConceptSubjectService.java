package no.fdk.referencedata.digdir.conceptsubjects;

import no.fdk.referencedata.core.ReferenceDataWriter;

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
import java.util.List;

@Service
@Slf4j
public class ConceptSubjectService {
    private final String dbSourceID = "concept-subjects-source";

    private final ConceptSubjectHarvester conceptSubjectHarvester;

    private final ReferenceDataWriter referenceDataWriter;

    private final ConceptSubjectRepository conceptSubjectRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ConceptSubjectService(
            ConceptSubjectHarvester conceptSubjectHarvester,
            RDFSourceRepository rdfSourceRepository,
            ConceptSubjectRepository conceptSubjectRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.conceptSubjectHarvester = conceptSubjectHarvester;
        this.rdfSourceRepository = rdfSourceRepository;
        this.conceptSubjectRepository = conceptSubjectRepository;
        this.referenceDataWriter = referenceDataWriter;
    }

    public boolean firstTime() {
        return conceptSubjectRepository.count() == 0;
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
            final List<ConceptSubject> items = new ArrayList<>();
            conceptSubjectHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} concept subjects", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(conceptSubjectHarvester.getModel(), RDFFormat.TURTLE));

            referenceDataWriter.replaceAll(conceptSubjectRepository, items, rdfSource);

        } catch (Exception e) {
            log.error("Unable to harvest concept subjects", e);
        }
    }
}
