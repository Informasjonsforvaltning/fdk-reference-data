package no.fdk.referencedata.digdir.roletype;

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
public class RoleTypeService {
    private final String dbSourceID = "role-types-source";

    private final RoleTypeHarvester roleTypeHarvester;

    private final ReferenceDataWriter referenceDataWriter;

    private final RoleTypeRepository roleTypeRepository;


    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public RoleTypeService(
            RoleTypeHarvester roleTypeHarvester,
            RoleTypeRepository roleTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.roleTypeHarvester = roleTypeHarvester;
        this.roleTypeRepository = roleTypeRepository;
        this.referenceDataWriter = referenceDataWriter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return roleTypeRepository.count() == 0;
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

            final List<RoleType> items = new ArrayList<>();
            roleTypeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} role-types", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(roleTypeHarvester.getModel(), RDFFormat.TURTLE));


            referenceDataWriter.replaceAll(roleTypeRepository, items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest role-types", e);
        }
    }
}
