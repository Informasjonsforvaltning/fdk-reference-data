package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@Slf4j
public class FylkeService implements SearchableReferenceData {
    private final String rdfSourceID = "fylke-source";

    private final FylkeHarvester fylkeHarvester;

    private final FylkeRepository fylkeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public FylkeService(FylkeHarvester fylkeHarvester,
                        FylkeRepository fylkeRepository,
                        RDFSourceRepository rdfSourceRepository,
                        HarvestSettingsRepository harvestSettingsRepository) {
        this.fylkeHarvester = fylkeHarvester;
        this.fylkeRepository = fylkeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return fylkeRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(rdfSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    private void addFylkeToModel(Fylke fylke, Model model) {
        Resource resource = model.createResource(fylke.getUri());
        resource.addProperty(RDF.type, DCTerms.Location);
        if (fylke.fylkesnavn != null) {
            resource.addProperty(DCTerms.title, fylke.fylkesnavn);
        }
        if (fylke.fylkesnummer != null) {
            resource.addProperty(DCTerms.identifier, fylke.fylkesnummer);
        }
    }

    public SearchAlternative getSearchType() {
        return SearchAlternative.ADMINISTRATIVE_ENHETER;
    }

    public Stream<SearchHit> search(String query) {
        return fylkeRepository.findByFylkesnavnContainingIgnoreCase(query)
                .map(Fylke::toSearchHit);
    }

    @Transactional
    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.GEONORGE_FYLKE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.GEONORGE_FYLKE.name())
                            .latestVersion("0")
                            .build());

            fylkeRepository.deleteAll();

            final AtomicInteger counter = new AtomicInteger(0);
            final Iterable<Fylke> iterable = fylkeHarvester.harvest().toIterable();
            iterable.forEach(item -> counter.getAndIncrement());
            log.info("Harvest and saving {} GeoNorge fylker", counter.get());
            fylkeRepository.saveAll(iterable);

            Model model = ModelFactory.createDefaultModel();
            model.setNsPrefix("dct", DCTerms.NS);
            iterable.forEach(item -> addFylkeToModel(item, model));

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(model, RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);

            settings.setLatestHarvestDate(LocalDateTime.now());
            harvestSettingsRepository.save(settings);
        } catch(Exception e) {
            log.error("Unable to harvest GeoNorge fylker", e);
        }
    }
}
