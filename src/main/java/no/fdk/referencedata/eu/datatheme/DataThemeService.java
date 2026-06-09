package no.fdk.referencedata.eu.datatheme;

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
public class DataThemeService {
    private final String dbSourceID = "data-theme-source";

    private final DataThemeHarvester dataThemeHarvester;

    private final DataThemeWriter dataThemeWriter;

    private final DataThemeRepository dataThemeRepository;

    private final RDFSourceRepository rdfSourceRepository;


    @Autowired
    public DataThemeService(
            DataThemeHarvester dataThemeHarvester,
            DataThemeRepository dataThemeRepository,
            RDFSourceRepository rdfSourceRepository,
            DataThemeWriter dataThemeWriter) {
        this.dataThemeHarvester = dataThemeHarvester;
        this.dataThemeRepository = dataThemeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.dataThemeWriter = dataThemeWriter;
    }

    public boolean firstTime() {
        return dataThemeRepository.count() == 0;
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

            final List<DataTheme> items = new ArrayList<>();
            dataThemeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} data-themes", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(dataThemeHarvester.getModel(), RDFFormat.TURTLE));


            dataThemeWriter.replaceAll(items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest data-themes", e);
        }
    }
}
