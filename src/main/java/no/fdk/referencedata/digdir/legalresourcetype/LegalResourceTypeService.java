package no.fdk.referencedata.digdir.legalresourcetype;

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
public class LegalResourceTypeService {
    private final String dbSourceID = "legal-resource-types-source";

    private final LegalResourceTypeHarvester legalResourceTypeHarvester;

    private final LegalResourceTypeRepository legalResourceTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public LegalResourceTypeService(LegalResourceTypeHarvester legalResourceTypeHarvester,
                                    LegalResourceTypeRepository legalResourceTypeRepository,
                                    RDFSourceRepository rdfSourceRepository,
                                    HarvestSettingsRepository harvestSettingsRepository) {
        this.legalResourceTypeHarvester = legalResourceTypeHarvester;
        this.legalResourceTypeRepository = legalResourceTypeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
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

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(legalResourceTypeHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.LEGAL_RESOURCE_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.LEGAL_RESOURCE_TYPE.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                legalResourceTypeRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<LegalResourceType> iterable = legalResourceTypeHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} legal-resource-types", counter.get());
                legalResourceTypeRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(legalResourceTypeHarvester.getVersion());
                harvestSettingsRepository.save(settings);

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(legalResourceTypeHarvester.getModel(), RDFFormat.TURTLE));
                rdfSourceRepository.save(rdfSource);
            }

        } catch(Exception e) {
            log.error("Unable to harvest legal-resource-types", e);
        }
    }
}
