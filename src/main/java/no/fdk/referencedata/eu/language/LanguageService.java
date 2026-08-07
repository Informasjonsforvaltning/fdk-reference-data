package no.fdk.referencedata.eu.language;

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
public class LanguageService implements SearchableReferenceData, HarvestableReferenceData {
    private final String dbSourceID = "language-source";

    private final LanguageHarvester languageHarvester;

    private final LanguageRepository languageRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public LanguageService(
            LanguageHarvester languageHarvester,
            LanguageRepository languageRepository,
            ReferenceDataServiceSupport support) {
        this.languageHarvester = languageHarvester;
        this.languageRepository = languageRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(languageRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    public SearchAlternative getSearchType() {
        return SearchAlternative.EU_LANGUAGES;
    }

    public Stream<SearchHit> search(String query) {
        return languageRepository.findByLabelContaining(query)
                .stream()
                .map(Language::toSearchHit);
    }

    public Stream<SearchHit> findByURIs(List<String> uris) {
        return languageRepository.findByUriIn(uris)
                .stream()
                .map(Language::toSearchHit);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(languageHarvester, languageRepository, dbSourceID, "languages");
    }
}
