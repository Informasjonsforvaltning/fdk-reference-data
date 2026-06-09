package no.fdk.referencedata.eu.datatheme;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataThemeWriter {

    private final DataThemeRepository dataThemeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public DataThemeWriter(
            DataThemeRepository dataThemeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.dataThemeRepository = dataThemeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<DataTheme> items, RDFSource rdfSource) {
        dataThemeRepository.deleteAll();
        dataThemeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
