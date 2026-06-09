package no.fdk.referencedata.eu.currency;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CurrencyService {
    private final String dbSourceID = "currency-source";

    private final CurrencyHarvester currencyHarvester;

    private final CurrencyWriter currencyWriter;
    private final CurrencyRepository currencyRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public CurrencyService(
            CurrencyHarvester currencyHarvester,
            CurrencyRepository currencyRepository,
            RDFSourceRepository rdfSourceRepository,
            CurrencyWriter currencyWriter) {
        this.currencyHarvester = currencyHarvester;
        this.currencyRepository = currencyRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.currencyWriter = currencyWriter;
    }

    public boolean firstTime() {
        return currencyRepository.count() == 0;
    }

    public Optional<Currency> getCurrency(String code) {
        return currencyRepository.findByCode(code);
    }

    public Currencies getCurrencies() {
        return Currencies.builder().currencies(
                currencyRepository.findAll().stream()
                        .sorted(Comparator.comparing(Currency::getUri))
                        .collect(Collectors.toList())).build();
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(dbSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    public void harvestAndSave() {
        try {

            final List<Currency> items = new ArrayList<>();
            currencyHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} currencies", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(currencyHarvester.getModel(), RDFFormat.TURTLE));


            currencyWriter.replaceAll(items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest currencies", e);
        }
    }
}
