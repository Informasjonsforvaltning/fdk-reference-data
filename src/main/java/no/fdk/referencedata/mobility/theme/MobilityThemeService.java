package no.fdk.referencedata.mobility.theme;

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
public class MobilityThemeService {
    private final String dbSourceID = "mobility-theme-source";

    private final MobilityThemeHarvester mobilityThemeHarvester;

    private final ReferenceDataWriter referenceDataWriter;

    private final MobilityThemeRepository mobilityThemeRepository;


    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MobilityThemeService(
            MobilityThemeHarvester mobilityThemeHarvester,
            MobilityThemeRepository mobilityThemeRepository,
            RDFSourceRepository rdfSourceRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.mobilityThemeHarvester = mobilityThemeHarvester;
        this.mobilityThemeRepository = mobilityThemeRepository;
        this.referenceDataWriter = referenceDataWriter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return mobilityThemeRepository.count() == 0;
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

            final List<MobilityTheme> items = new ArrayList<>();
            mobilityThemeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} mobility themes", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(mobilityThemeHarvester.getModel(), RDFFormat.TURTLE));


            referenceDataWriter.replaceAll(mobilityThemeRepository, items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest mobility themes", e);
        }
    }
}
