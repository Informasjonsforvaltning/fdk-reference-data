package no.fdk.referencedata.mobility.conditions;

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
public class MobilityConditionService {
    private final String dbSourceID = "mobility-condition-source";

    private final MobilityConditionHarvester mobilityConditionHarvester;

    private final MobilityConditionWriter mobilityConditionWriter;

    private final MobilityConditionRepository mobilityConditionRepository;


    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MobilityConditionService(
            MobilityConditionHarvester mobilityConditionHarvester,
            MobilityConditionRepository mobilityConditionRepository,
            RDFSourceRepository rdfSourceRepository,
            MobilityConditionWriter mobilityConditionWriter) {
        this.mobilityConditionHarvester = mobilityConditionHarvester;
        this.mobilityConditionRepository = mobilityConditionRepository;
        this.mobilityConditionWriter = mobilityConditionWriter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return mobilityConditionRepository.count() == 0;
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

            final List<MobilityCondition> items = new ArrayList<>();
            mobilityConditionHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} mobility conditions", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(mobilityConditionHarvester.getModel(), RDFFormat.TURTLE));


            mobilityConditionWriter.replaceAll(items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest mobility conditions", e);
        }
    }
}
