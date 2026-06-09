package no.fdk.referencedata.mobility.theme;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class MobilityThemeWriter {

    private final MobilityThemeRepository mobilityThemeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MobilityThemeWriter(
            MobilityThemeRepository mobilityThemeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.mobilityThemeRepository = mobilityThemeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<MobilityTheme> items, RDFSource rdfSource) {
        mobilityThemeRepository.deleteAll();
        mobilityThemeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
