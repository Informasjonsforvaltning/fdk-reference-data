package no.fdk.referencedata.eu.currency;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CurrencyWriter {

    private final CurrencyRepository currencyRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public CurrencyWriter(
            CurrencyRepository currencyRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.currencyRepository = currencyRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<Currency> items, RDFSource rdfSource) {
        currencyRepository.deleteAll();
        currencyRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
