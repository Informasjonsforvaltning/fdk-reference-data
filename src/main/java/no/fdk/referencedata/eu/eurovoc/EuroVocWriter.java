package no.fdk.referencedata.eu.eurovoc;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class EuroVocWriter {

    private final EuroVocRepository euroVocRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public EuroVocWriter(
            EuroVocRepository euroVocRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.euroVocRepository = euroVocRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<EuroVoc> items, RDFSource rdfSource) {
        euroVocRepository.deleteAll();
        euroVocRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
