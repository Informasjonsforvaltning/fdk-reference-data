package no.fdk.referencedata.digdir.legalresourcetype;

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
public class LegalResourceTypeService {
    private final String dbSourceID = "legal-resource-types-source";

    private final LegalResourceTypeHarvester legalResourceTypeHarvester;

    private final LegalResourceTypeWriter legalResourceTypeWriter;

    private final LegalResourceTypeRepository legalResourceTypeRepository;


    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public LegalResourceTypeService(
            LegalResourceTypeHarvester legalResourceTypeHarvester,
            LegalResourceTypeRepository legalResourceTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            LegalResourceTypeWriter legalResourceTypeWriter) {
        this.legalResourceTypeHarvester = legalResourceTypeHarvester;
        this.legalResourceTypeRepository = legalResourceTypeRepository;
        this.legalResourceTypeWriter = legalResourceTypeWriter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return legalResourceTypeRepository.count() == 0;
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

            final List<LegalResourceType> items = new ArrayList<>();
            legalResourceTypeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} legal-resource-types", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(legalResourceTypeHarvester.getModel(), RDFFormat.TURTLE));


            legalResourceTypeWriter.replaceAll(items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest legal-resource-types", e);
        }
    }
}
