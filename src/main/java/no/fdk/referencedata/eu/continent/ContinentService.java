package no.fdk.referencedata.eu.continent;

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
public class ContinentService implements SearchableReferenceData, HarvestableReferenceData {
    private final String dbSourceID = "continent-source";

    private final ContinentHarvester continentHarvester;

    private final ContinentRepository continentRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public ContinentService(
            ContinentHarvester continentHarvester,
            ContinentRepository continentRepository,
            ReferenceDataServiceSupport support) {
        this.continentHarvester = continentHarvester;
        this.continentRepository = continentRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(continentRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
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

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(continentHarvester, continentRepository, dbSourceID, "continents");
    }
}
