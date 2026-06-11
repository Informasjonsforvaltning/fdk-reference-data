package no.fdk.referencedata.eu.language;

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
public class LanguageService implements SearchableReferenceData {
    private final String dbSourceID = "language-source";

    private final LanguageHarvester languageHarvester;

    private final LanguageWriter languageWriter;

    private final LanguageRepository languageRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public LanguageService(
            LanguageHarvester languageHarvester,
            LanguageRepository languageRepository,
            RDFSourceRepository rdfSourceRepository,
            LanguageWriter languageWriter) {
        this.languageHarvester = languageHarvester;
        this.languageRepository = languageRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.languageWriter = languageWriter;
    }

    public boolean firstTime() {
        return languageRepository.count() == 0;
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

    public void harvestAndSave() {
        try {
            final List<Language> items = new ArrayList<>();
            languageHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} languages", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(languageHarvester.getModel(), RDFFormat.TURTLE));

            languageWriter.replaceAll(items, rdfSource);

        } catch (Exception e) {
            log.error("Unable to harvest languages", e);
        }
    }
}
