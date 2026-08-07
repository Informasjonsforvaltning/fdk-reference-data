package no.fdk.referencedata.eu.datasettype;

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
public class DatasetTypeService {
    private final String dbSourceID = "dataset-types-source";

    private final DatasetTypeHarvester datasetTypeHarvester;

    private final ReferenceDataWriter referenceDataWriter;

    private final DatasetTypeRepository datasetTypeRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public DatasetTypeService(
            DatasetTypeHarvester datasetTypeHarvester,
            DatasetTypeRepository datasetTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.datasetTypeHarvester = datasetTypeHarvester;
        this.datasetTypeRepository = datasetTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.referenceDataWriter = referenceDataWriter;
    }

    public boolean firstTime() {
        return datasetTypeRepository.count() == 0;
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

            final List<DatasetType> items = new ArrayList<>();
            datasetTypeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} dataset-types", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(datasetTypeHarvester.getModel(), RDFFormat.TURTLE));


            referenceDataWriter.replaceAll(datasetTypeRepository, items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest dataset-types", e);
        }
    }
}
