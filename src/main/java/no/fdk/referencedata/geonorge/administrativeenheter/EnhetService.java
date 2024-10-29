package no.fdk.referencedata.geonorge.administrativeenheter;

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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@Slf4j
public class EnhetService implements SearchableReferenceData {
    private final String rdfSourceID = "administrative-enheter-source";

    private final EnhetHarvester enhetHarvester;

    private final EnhetRepository enhetRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public EnhetService(EnhetHarvester enhetHarvester,
                        EnhetRepository enhetRepository,
                        RDFSourceRepository rdfSourceRepository,
                        HarvestSettingsRepository harvestSettingsRepository) {
        this.enhetHarvester = enhetHarvester;
        this.enhetRepository = enhetRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return enhetRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(rdfSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    private void addEnhetToModel(Enhet enhet, Model model) {
        Resource resource = model.createResource(enhet.getUri());
        resource.addProperty(RDF.type, DCTerms.Location);
        if (enhet.name != null) {
            resource.addProperty(DCTerms.title, enhet.name);
        }
        if (enhet.code != null) {
            resource.addProperty(DCTerms.identifier, enhet.code);
        }
    }

    public SearchAlternative getSearchType() {
        return SearchAlternative.ADMINISTRATIVE_ENHETER;
    }

    public Stream<SearchHit> search(String query) {
        return enhetRepository.findByNameContainingIgnoreCase(query)
                .map(Enhet::toSearchHit);
    }

    public Stream<SearchHit> findByURIs(List<String> uris) {
        return enhetRepository.findByUriIn(uris)
                .map(Enhet::toSearchHit);
    }

    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.ADMINISTRATIVE_ENHETER.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.ADMINISTRATIVE_ENHETER.name())
                            .latestVersion("0")
                            .build());

            enhetRepository.deleteAll();

            final AtomicInteger counter = new AtomicInteger(0);
            final Iterable<Enhet> iterable = enhetHarvester.harvest().toIterable();
            iterable.forEach(item -> counter.getAndIncrement());
            log.info("Harvest and saving {} administrative enheter", counter.get());
            enhetRepository.saveAll(iterable);

            Model model = ModelFactory.createDefaultModel();
            model.setNsPrefix("dct", DCTerms.NS);
            iterable.forEach(item -> addEnhetToModel(item, model));

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(model, RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);

            settings.setLatestHarvestDate(LocalDateTime.now());
            harvestSettingsRepository.save(settings);
        } catch(Exception e) {
            log.error("Unable to harvest administrative enheter", e);
        }
    }
}
