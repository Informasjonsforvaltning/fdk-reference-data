package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

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
public class KommuneService implements SearchableReferenceData {
    private final String rdfSourceID = "kommune-source";

    private final KommuneHarvester kommuneHarvester;

    private final KommuneRepository kommuneRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public KommuneService(KommuneHarvester kommuneHarvester,
                          KommuneRepository kommuneRepository,
                          RDFSourceRepository rdfSourceRepository,
                          HarvestSettingsRepository harvestSettingsRepository) {
        this.kommuneHarvester = kommuneHarvester;
        this.kommuneRepository = kommuneRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return kommuneRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(rdfSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    private void addKommuneToModel(Kommune kommune, Model model) {
        Resource resource = model.createResource(kommune.getUri());
        resource.addProperty(RDF.type, DCTerms.Location);
        if (kommune.kommunenavn != null) {
            resource.addProperty(DCTerms.title, kommune.kommunenavn);
        }
        if (kommune.kommunenavnNorsk != null) {
            resource.addProperty(DCTerms.title, kommune.kommunenavnNorsk, "nb");
        }
        if (kommune.kommunenummer != null) {
            resource.addProperty(DCTerms.identifier, kommune.kommunenummer);
        }
    }

    public SearchAlternative getSearchType() {
        return SearchAlternative.ADMINISTRATIVE_ENHETER;
    }

    public Stream<SearchHit> search(String query) {
        return kommuneRepository.findByKommunenavnNorskContainingIgnoreCase(query)
                .map(Kommune::toSearchHit);
    }

    @Transactional
    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.GEONORGE_KOMMUNE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.GEONORGE_KOMMUNE.name())
                            .latestVersion("0")
                            .build());

            kommuneRepository.deleteAll();

            final AtomicInteger counter = new AtomicInteger(0);
            final Iterable<Kommune> iterable = kommuneHarvester.harvest().toIterable();
            iterable.forEach(item -> counter.getAndIncrement());
            log.info("Harvest and saving {} GeoNorge kommuner", counter.get());
            kommuneRepository.saveAll(iterable);

            Model model = ModelFactory.createDefaultModel();
            model.setNsPrefix("dct", DCTerms.NS);
            iterable.forEach(item -> addKommuneToModel(item, model));

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(model, RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);

            settings.setLatestHarvestDate(LocalDateTime.now());
            harvestSettingsRepository.save(settings);
        } catch(Exception e) {
            log.error("Unable to harvest GeoNorge kommuner", e);
        }
    }
}
