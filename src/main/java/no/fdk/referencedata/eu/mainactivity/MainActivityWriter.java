package no.fdk.referencedata.eu.mainactivity;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class MainActivityWriter {

    private final MainActivityRepository mainActivityRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MainActivityWriter(
            MainActivityRepository mainActivityRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.mainActivityRepository = mainActivityRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<MainActivity> items, RDFSource rdfSource) {
        mainActivityRepository.deleteAll();
        mainActivityRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
