package no.fdk.referencedata.eu.country;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.search.SearchAlternative;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchableReferenceData;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CountryService implements SearchableReferenceData, HarvestableReferenceData {
    private final String dbSourceID = "country-source";

    private final CountryHarvester countryHarvester;

    private final CountryRepository countryRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public CountryService(
            CountryHarvester countryHarvester,
            CountryRepository countryRepository,
            ReferenceDataServiceSupport support) {
        this.countryHarvester = countryHarvester;
        this.countryRepository = countryRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(countryRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
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

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(countryHarvester, countryRepository, dbSourceID, "countries");
    }
}
