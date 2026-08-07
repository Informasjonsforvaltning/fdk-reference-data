package no.fdk.referencedata.eu.continent;

import no.fdk.referencedata.core.ReferenceDataWriter;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.search.SearchAlternative;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchableReferenceData;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class ContinentService implements SearchableReferenceData {
    private final String dbSourceID = "continent-source";

    private final ContinentHarvester continentHarvester;

    private final ReferenceDataWriter referenceDataWriter;

    private final ContinentRepository continentRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ContinentService(
            ContinentHarvester continentHarvester,
            ContinentRepository continentRepository,
            RDFSourceRepository rdfSourceRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.continentHarvester = continentHarvester;
        this.continentRepository = continentRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.referenceDataWriter = referenceDataWriter;
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

            final List<Continent> items = new ArrayList<>();
            continentHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} continents", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(continentHarvester.getModel(), RDFFormat.TURTLE));


            referenceDataWriter.replaceAll(continentRepository, items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest continents", e);
        }
    }
}
