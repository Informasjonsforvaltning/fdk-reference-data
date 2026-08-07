package no.fdk.referencedata.eu.country;

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
public class CountryService implements SearchableReferenceData {
    private final String dbSourceID = "country-source";

    private final CountryHarvester countryHarvester;

    private final ReferenceDataWriter referenceDataWriter;

    private final CountryRepository countryRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public CountryService(
            CountryHarvester countryHarvester,
            CountryRepository countryRepository,
            RDFSourceRepository rdfSourceRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.countryHarvester = countryHarvester;
        this.countryRepository = countryRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.referenceDataWriter = referenceDataWriter;
    }

    public boolean firstTime() {
        return countryRepository.count() == 0;
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
        return countryRepository.findByLabelContaining(query)
                .stream()
                .map(Country::toSearchHit);
    }

    public Stream<SearchHit> findByURIs(List<String> uris) {
        return countryRepository.findByUriIn(uris)
                .stream()
                .map(Country::toSearchHit);
    }

    public void harvestAndSave() {
        try {

            final List<Country> items = new ArrayList<>();
            countryHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} countries", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(countryHarvester.getModel(), RDFFormat.TURTLE));


            referenceDataWriter.replaceAll(countryRepository, items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest countries", e);
        }
    }
}
