package no.fdk.referencedata.eu.distributiontype;

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
public class DistributionTypeService {
    private final String dbSourceID = "distribution-types-source";

    private final DistributionTypeHarvester distributionTypeHarvester;

    private final DistributionTypeWriter distributionTypeWriter;

    private final DistributionTypeRepository distributionTypeRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public DistributionTypeService(
            DistributionTypeHarvester distributionTypeHarvester,
            DistributionTypeRepository distributionTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            DistributionTypeWriter distributionTypeWriter) {
        this.distributionTypeHarvester = distributionTypeHarvester;
        this.distributionTypeRepository = distributionTypeRepository;
        this.distributionTypeWriter = distributionTypeWriter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return distributionTypeRepository.count() == 0;
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

            final List<DistributionType> items = new ArrayList<>();
            distributionTypeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} distribution-types", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(distributionTypeHarvester.getModel(), RDFFormat.TURTLE));


            distributionTypeWriter.replaceAll(items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest distribution-types", e);
        }
    }
}
