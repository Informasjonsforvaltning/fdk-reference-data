package no.fdk.referencedata.eu.highvaluecategories;

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
public class HighValueCategoryService {
    private final String dbSourceID = "high-value-categories-source";

    private final HighValueCategoriesHarvester highValueCategoriesHarvester;

    private final ReferenceDataWriter referenceDataWriter;

    private final HighValueCategoryRepository highValueCategoryRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public HighValueCategoryService(
            HighValueCategoriesHarvester highValueCategoriesHarvester,
            HighValueCategoryRepository highValueCategoryRepository,
            RDFSourceRepository rdfSourceRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.highValueCategoriesHarvester = highValueCategoriesHarvester;
        this.highValueCategoryRepository = highValueCategoryRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.referenceDataWriter = referenceDataWriter;
    }

    public boolean firstTime() {
        return highValueCategoryRepository.count() == 0;
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

            final List<HighValueCategory> items = new ArrayList<>();
            highValueCategoriesHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} high-value categories", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(highValueCategoriesHarvester.getModel(), RDFFormat.TURTLE));


            referenceDataWriter.replaceAll(highValueCategoryRepository, items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest high-value categories", e);
        }
    }
}
