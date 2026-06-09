package no.fdk.referencedata.eu.country;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CountryWriter {

    private final CountryRepository countryRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public CountryWriter(
            CountryRepository countryRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.countryRepository = countryRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<Country> items, RDFSource rdfSource) {
        countryRepository.deleteAll();
        countryRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
