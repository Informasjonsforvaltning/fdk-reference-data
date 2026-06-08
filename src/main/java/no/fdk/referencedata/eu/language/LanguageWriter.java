package no.fdk.referencedata.eu.language;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class LanguageWriter {

    private final LanguageRepository languageRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public LanguageWriter(
            LanguageRepository languageRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.languageRepository = languageRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<Language> items, RDFSource rdfSource) {
        languageRepository.deleteAll();
        languageRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
