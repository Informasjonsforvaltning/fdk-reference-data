package no.fdk.referencedata.eu.eurovoc;

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
public class EuroVocService {
    private final String dbSourceID = "eurovoc-source";

    private final EuroVocHarvester euroVocHarvester;

    private final EuroVocWriter euroVocWriter;

    private final EuroVocRepository euroVocRepository;

    private final RDFSourceRepository rdfSourceRepository;


    @Autowired
    public EuroVocService(
            EuroVocHarvester euroVocHarvester,
            EuroVocRepository euroVocRepository,
            RDFSourceRepository rdfSourceRepository,
            EuroVocWriter euroVocWriter) {
        this.euroVocHarvester = euroVocHarvester;
        this.euroVocRepository = euroVocRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.euroVocWriter = euroVocWriter;
    }

    public boolean firstTime() {
        return euroVocRepository.count() == 0;
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

            final List<EuroVoc> items = new ArrayList<>();
            euroVocHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} eurovocs", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(euroVocHarvester.getModel(), RDFFormat.TURTLE));


            euroVocWriter.replaceAll(items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest eurovoc", e);
        }
    }
}
