package no.fdk.referencedata.eu.continent;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ContinentWriter {

    private final ContinentRepository continentRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ContinentWriter(
            ContinentRepository continentRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.continentRepository = continentRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<Continent> items, RDFSource rdfSource) {
        continentRepository.deleteAll();
        continentRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
