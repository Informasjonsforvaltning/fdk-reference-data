package no.fdk.referencedata.eu.frequency;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class FrequencyWriter {

    private final FrequencyRepository frequencyRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public FrequencyWriter(
            FrequencyRepository frequencyRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.frequencyRepository = frequencyRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<Frequency> items, RDFSource rdfSource) {
        frequencyRepository.deleteAll();
        frequencyRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
