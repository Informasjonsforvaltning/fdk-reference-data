package no.fdk.referencedata.eu.filetype;

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
public class FileTypeService implements SearchableReferenceData {
    private final String dbSourceID = "file-types-source";

    private final FileTypeHarvester fileTypeHarvester;

    private final FileTypeRepository fileTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public FileTypeService(FileTypeHarvester fileTypeHarvester,
                           FileTypeRepository fileTypeRepository,
                           RDFSourceRepository rdfSourceRepository,
                           HarvestSettingsRepository harvestSettingsRepository) {
        this.fileTypeHarvester = fileTypeHarvester;
        this.fileTypeRepository = fileTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return fileTypeRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(dbSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    public SearchAlternative getSearchType() {
        return SearchAlternative.EU_FILE_TYPES;
    }

    public Stream<SearchHit> search(String query) {
        return fileTypeRepository.findByCodeContainingIgnoreCase(query)
                .map(FileType::toSearchHit);
    }

    public Stream<SearchHit> findByURIs(List<String> uris) {
        return fileTypeRepository.findByUriIn(uris)
                .map(FileType::toSearchHit);
    }

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(fileTypeHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.FILE_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.FILE_TYPE.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                fileTypeRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<FileType> iterable = fileTypeHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} file-types", counter.get());
                fileTypeRepository.saveAll(iterable);

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(fileTypeHarvester.getModel(), RDFFormat.TURTLE));
                rdfSourceRepository.save(rdfSource);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(fileTypeHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest file-types", e);
        }
    }
}
