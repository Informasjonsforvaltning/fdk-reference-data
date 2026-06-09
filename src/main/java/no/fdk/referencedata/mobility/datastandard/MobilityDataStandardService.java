package no.fdk.referencedata.mobility.datastandard;

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
public class MobilityDataStandardService {
    private final String dbSourceID = "mobility-data-standard-source";

    private final MobilityDataStandardHarvester mobilityDataStandardHarvester;

    private final MobilityDataStandardWriter mobilityDataStandardWriter;

    private final MobilityDataStandardRepository mobilityDataStandardRepository;


    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MobilityDataStandardService(
            MobilityDataStandardHarvester mobilityDataStandardHarvester,
            MobilityDataStandardRepository mobilityDataStandardRepository,
            RDFSourceRepository rdfSourceRepository,
            MobilityDataStandardWriter mobilityDataStandardWriter) {
        this.mobilityDataStandardHarvester = mobilityDataStandardHarvester;
        this.mobilityDataStandardRepository = mobilityDataStandardRepository;
        this.mobilityDataStandardWriter = mobilityDataStandardWriter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return mobilityDataStandardRepository.count() == 0;
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

            final List<MobilityDataStandard> items = new ArrayList<>();
            mobilityDataStandardHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} mobility data standards", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(mobilityDataStandardHarvester.getModel(), RDFFormat.TURTLE));


            mobilityDataStandardWriter.replaceAll(items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest mobility data standards", e);
        }
    }
}
