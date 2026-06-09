package no.fdk.referencedata.eu.continent;

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
public class ContinentService implements SearchableReferenceData {
    private final String dbSourceID = "continent-source";

    private final ContinentHarvester continentHarvester;

    private final ContinentWriter continentWriter;

    private final ContinentRepository continentRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ContinentService(
            ContinentHarvester continentHarvester,
            ContinentRepository continentRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            ContinentWriter continentWriter) {
        this.continentHarvester = continentHarvester;
        this.continentRepository = continentRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.continentWriter = continentWriter;
    }

    public boolean firstTime() {
        return continentRepository.count() == 0;
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
        return SearchAlternative.EU_LOCATIONS;
    }

    public Stream<SearchHit> search(String query) {
        return continentRepository.findByLabelContaining(query)
                .stream()
                .map(Continent::toSearchHit);
    }

    public Stream<SearchHit> findByURIs(List<String> uris) {
        return continentRepository.findByUriIn(uris)
                .stream()
                .map(Continent::toSearchHit);
    }

    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.CONTINENT.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.CONTINENT.name())
                            .latestVersion("0")
                            .build());

            final List<Continent> items = new ArrayList<>();
            continentHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} continents", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(continentHarvester.getModel(), RDFFormat.TURTLE));

            settings.setLatestHarvestDate(LocalDateTime.now());
            settings.setLatestVersion(continentHarvester.getVersion());

            continentWriter.replaceAll(items, rdfSource, settings);
        } catch (Exception e) {
            log.error("Unable to harvest continents", e);
        }
    }
}
