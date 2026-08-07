package no.fdk.referencedata.digdir.audiencetype;

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
public class AudienceTypeService {
    private final String dbSourceID = "audience-types-source";

    private final AudienceTypeHarvester audienceTypeHarvester;

    private final ReferenceDataWriter referenceDataWriter;

    private final AudienceTypeRepository audienceTypeRepository;


    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public AudienceTypeService(
            AudienceTypeHarvester audienceTypeHarvester,
            AudienceTypeRepository audienceTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.audienceTypeHarvester = audienceTypeHarvester;
        this.audienceTypeRepository = audienceTypeRepository;
        this.referenceDataWriter = referenceDataWriter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return audienceTypeRepository.count() == 0;
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

            final List<AudienceType> items = new ArrayList<>();
            audienceTypeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} audience-types", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(audienceTypeHarvester.getModel(), RDFFormat.TURTLE));


            referenceDataWriter.replaceAll(audienceTypeRepository, items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest audience-types", e);
        }
    }
}
