package no.fdk.referencedata.digdir.roletype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RoleTypeService {
    private final String dbSourceID = "role-types-source";

    private final RoleTypeHarvester roleTypeHarvester;

    private final RoleTypeWriter roleTypeWriter;

    private final RoleTypeRepository roleTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public RoleTypeService(
            RoleTypeHarvester roleTypeHarvester,
            RoleTypeRepository roleTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            RoleTypeWriter roleTypeWriter) {
        this.roleTypeHarvester = roleTypeHarvester;
        this.roleTypeRepository = roleTypeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.roleTypeWriter = roleTypeWriter;
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
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.ROLE_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.ROLE_TYPE.name())
                            .latestVersion("0")
                            .build());

            final List<RoleType> items = new ArrayList<>();
            roleTypeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} role-types", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(roleTypeHarvester.getModel(), RDFFormat.TURTLE));

            settings.setLatestHarvestDate(LocalDateTime.now());
            settings.setLatestVersion(roleTypeHarvester.getVersion());

            roleTypeWriter.replaceAll(items, rdfSource, settings);
        } catch (Exception e) {
            log.error("Unable to harvest role-types", e);
        }
    }
}
