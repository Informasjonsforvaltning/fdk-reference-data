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
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class LicenceService implements SearchableReferenceData {
    private final String dbSourceID = "licences-source";

    private final LicenceHarvester licenceHarvester;

    private final LicenceWriter licenceWriter;

    private final LicenceRepository licenceRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public LicenceService(
            LicenceHarvester licenceHarvester,
            LicenceRepository licenceRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            LicenceWriter licenceWriter) {
        this.licenceHarvester = licenceHarvester;
        this.licenceRepository = licenceRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.licenceWriter = licenceWriter;
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

    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.LICENCE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.LICENCE.name())
                            .latestVersion("0")
                            .build());

            final List<Licence> items = new ArrayList<>();
            licenceHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} licences", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(licenceHarvester.getModel(), RDFFormat.TURTLE));

            settings.setLatestHarvestDate(LocalDateTime.now());
            settings.setLatestVersion(licenceHarvester.getVersion());

            licenceWriter.replaceAll(items, rdfSource, settings);
        } catch (Exception e) {
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
