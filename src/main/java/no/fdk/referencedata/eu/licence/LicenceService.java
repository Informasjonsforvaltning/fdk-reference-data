package no.fdk.referencedata.eu.licence;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.search.SearchAlternative;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchableReferenceData;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@Slf4j
public class LicenceService implements SearchableReferenceData {
    private final String dbSourceID = "licences-source";

    private final LicenceHarvester licenceHarvester;

    private final LicenceRepository licenceRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public LicenceService(LicenceHarvester licenceHarvester,
                          LicenceRepository licenceRepository,
                          RDFSourceRepository rdfSourceRepository,
                          HarvestSettingsRepository harvestSettingsRepository) {
        this.licenceHarvester = licenceHarvester;
        this.licenceRepository = licenceRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return licenceRepository.count() == 0;
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
            final Version latestVersion = new Version(licenceHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.LICENCE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.LICENCE.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                licenceRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<Licence> iterable = licenceHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} licences", counter.get());
                licenceRepository.saveAll(iterable);

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(licenceHarvester.getModel(), RDFFormat.TURTLE));
                rdfSourceRepository.save(rdfSource);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(licenceHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest licences", e);
        }
    }

    @Override
    public Stream<SearchHit> search(String query) {
        return Stream.empty();
    }

    @Override
    public Stream<SearchHit> findByURIs(List<String> uris) {
        return Stream.empty();
    }

    @Override
    public SearchAlternative getSearchType() {
        return null;
    }
}
