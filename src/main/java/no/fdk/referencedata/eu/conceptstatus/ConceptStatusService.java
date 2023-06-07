package no.fdk.referencedata.eu.conceptstatus;

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
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class ConceptStatusService {
    private final String dbSourceID = "concept-status-source";

    private final ConceptStatusHarvester conceptStatusHarvester;
    private final ConceptStatusRepository conceptStatusRepository;
    private final RDFSourceRepository rdfSourceRepository;
    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public ConceptStatusService(ConceptStatusHarvester conceptStatusHarvester,
                                ConceptStatusRepository conceptStatusRepository,
                                RDFSourceRepository rdfSourceRepository,
                                HarvestSettingsRepository harvestSettingsRepository) {
        this.conceptStatusHarvester = conceptStatusHarvester;
        this.conceptStatusRepository = conceptStatusRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return conceptStatusRepository.count() == 0;
    }

    public Optional<ConceptStatus> getConceptStatus(String code) {
        return conceptStatusRepository.findByCode(code);
    }

    public ConceptStatuses getConceptStatuses() {
        return ConceptStatuses.builder().conceptStatuses(
                StreamSupport.stream(conceptStatusRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(ConceptStatus::getUri))
                        .collect(Collectors.toList())).build();
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
            final Version latestVersion = new Version(conceptStatusHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.CONCEPT_STATUS.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.CONCEPT_STATUS.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                conceptStatusRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<ConceptStatus> iterable = conceptStatusHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} concept status", counter.get());
                conceptStatusRepository.saveAll(iterable);

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(conceptStatusHarvester.getModel(), RDFFormat.TURTLE));
                rdfSourceRepository.save(rdfSource);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(conceptStatusHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest concept statuses", e);
        }
    }
}
