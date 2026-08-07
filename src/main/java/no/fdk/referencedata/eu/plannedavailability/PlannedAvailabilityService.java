package no.fdk.referencedata.eu.plannedavailability;

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
public class PlannedAvailabilityService {
    private final String dbSourceID = "planned-availability-source";

    private final PlannedAvailabilityHarvester plannedAvailabilityHarvester;

    private final ReferenceDataWriter referenceDataWriter;

    private final PlannedAvailabilityRepository plannedAvailabilityRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public PlannedAvailabilityService(
            PlannedAvailabilityHarvester plannedAvailabilityHarvester,
            PlannedAvailabilityRepository plannedAvailabilityRepository,
            RDFSourceRepository rdfSourceRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.plannedAvailabilityHarvester = plannedAvailabilityHarvester;
        this.plannedAvailabilityRepository = plannedAvailabilityRepository;
        this.referenceDataWriter = referenceDataWriter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return plannedAvailabilityRepository.count() == 0;
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

            final List<PlannedAvailability> items = new ArrayList<>();
            plannedAvailabilityHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} planned availabilities", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(plannedAvailabilityHarvester.getModel(), RDFFormat.TURTLE));


            referenceDataWriter.replaceAll(plannedAvailabilityRepository, items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest planned availabilities", e);
        }
    }
}
