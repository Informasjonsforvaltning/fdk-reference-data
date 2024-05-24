package no.fdk.referencedata.digdir.relationshipwithsourcetype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import no.fdk.referencedata.util.Version;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class RelationshipWithSourceTypeService {
    private final String dbSourceID = "relationship-with-source-types-source";

    private final RelationshipWithSourceTypeHarvester relationshipWithSourceTypeHarvester;

    private final RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public RelationshipWithSourceTypeService(RelationshipWithSourceTypeHarvester relationshipWithSourceTypeHarvester,
                                             RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository,
                                             RDFSourceRepository rdfSourceRepository,
                                             HarvestSettingsRepository harvestSettingsRepository) {
        this.relationshipWithSourceTypeHarvester = relationshipWithSourceTypeHarvester;
        this.relationshipWithSourceTypeRepository = relationshipWithSourceTypeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return relationshipWithSourceTypeRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(dbSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(relationshipWithSourceTypeHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.RELATIONSHIP_WITH_SOURCE_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.RELATIONSHIP_WITH_SOURCE_TYPE.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                relationshipWithSourceTypeRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<RelationshipWithSourceType> iterable = relationshipWithSourceTypeHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} relationship-with-source-types", counter.get());
                relationshipWithSourceTypeRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(relationshipWithSourceTypeHarvester.getVersion());
                harvestSettingsRepository.save(settings);

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(relationshipWithSourceTypeHarvester.getModel(), RDFFormat.TURTLE));
                rdfSourceRepository.save(rdfSource);
            }

        } catch(Exception e) {
            log.error("Unable to harvest relationship-with-source-types", e);
        }
    }
}
