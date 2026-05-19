package no.fdk.referencedata.geonames;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.search.SearchAlternative;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchableReferenceData;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@Slf4j
public class GeonamesService implements SearchableReferenceData {
    private final String rdfSourceID = "geonames-source";

    private final GeonamesHarvester geonamesHarvester;
    private final GeonamesFylkeRepository geonamesFylkeRepository;
    private final GeonamesKommuneRepository geonamesKommuneRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public GeonamesService(GeonamesHarvester geonamesHarvester,
                           GeonamesFylkeRepository geonamesFylkeRepository,
                           GeonamesKommuneRepository geonamesKommuneRepository,
                           RDFSourceRepository rdfSourceRepository) {
        this.geonamesHarvester = geonamesHarvester;
        this.geonamesFylkeRepository = geonamesFylkeRepository;
        this.geonamesKommuneRepository = geonamesKommuneRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return geonamesFylkeRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(rdfSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    public SearchAlternative getSearchType() {
        return SearchAlternative.GEONAMES;
    }

    public Stream<SearchHit> search(String query) {
        return Stream.concat(
                geonamesFylkeRepository.findByNameContainingIgnoreCase(query).stream().map(GeonamesFylke::toSearchHit),
                geonamesKommuneRepository.findByNameContainingIgnoreCase(query).stream().map(GeonamesKommune::toSearchHit)
        );
    }

    public Stream<SearchHit> findByURIs(List<String> uris) {
        return Stream.concat(
                geonamesFylkeRepository.findByUriIn(uris).stream().map(GeonamesFylke::toSearchHit),
                geonamesKommuneRepository.findByUriIn(uris).stream().map(GeonamesKommune::toSearchHit)
        );
    }

    private void addLocationToModel(GeonamesFylke fylke, Model model) {
        Resource resource = model.createResource(fylke.getUri());
        resource.addProperty(RDF.type, DCTerms.Location);
        if (fylke.name != null) {
            resource.addProperty(DCTerms.title, fylke.name, Language.NORWEGIAN.code());
        }
        if (fylke.geonameId != null) {
            resource.addProperty(DCTerms.identifier, fylke.geonameId);
        }
    }

    private void addLocationToModel(GeonamesKommune kommune, Model model) {
        Resource resource = model.createResource(kommune.getUri());
        resource.addProperty(RDF.type, DCTerms.Location);
        if (kommune.name != null) {
            resource.addProperty(DCTerms.title, kommune.name, Language.NORWEGIAN.code());
        }
        if (kommune.geonameId != null) {
            resource.addProperty(DCTerms.identifier, kommune.geonameId);
        }
    }

    @Transactional
    public void harvestAndSave() {
        try {
            geonamesKommuneRepository.deleteAll();
            geonamesFylkeRepository.deleteAll();

            Model model = ModelFactory.createDefaultModel();
            model.setNsPrefix("dct", DCTerms.NS);

            List<GeonamesFylke> fylker = geonamesHarvester.harvestFylker().collectList().block();
            if (fylker == null || fylker.isEmpty()) {
                log.warn("No Norwegian counties harvested from GeoNames");
                return;
            }

            geonamesFylkeRepository.saveAll(fylker);
            log.info("Harvested and saved {} Norwegian counties from GeoNames", fylker.size());
            fylker.forEach(item -> addLocationToModel(item, model));

            AtomicInteger kommuneCount = new AtomicInteger(0);
            for (GeonamesFylke fylke : fylker) {
                try {
                    List<GeonamesKommune> kommuner = geonamesHarvester
                            .harvestKommunerForFylke(fylke.getGeonameId())
                            .collectList()
                            .block();
                    if (kommuner != null && !kommuner.isEmpty()) {
                        geonamesKommuneRepository.saveAll(kommuner);
                        kommuner.forEach(item -> addLocationToModel(item, model));
                        kommuneCount.addAndGet(kommuner.size());
                    }
                } catch (Exception e) {
                    log.error("Failed to harvest districts for county {} ({})", fylke.getName(), fylke.getGeonameId(), e);
                }
            }

            log.info("Harvested and saved {} Norwegian districts from GeoNames", kommuneCount.get());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(model, RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest GeoNames data", e);
        }
    }
}
