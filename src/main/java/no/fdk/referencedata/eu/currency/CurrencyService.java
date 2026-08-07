package no.fdk.referencedata.eu.currency;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CurrencyService implements HarvestableReferenceData {
    private final String dbSourceID = "currency-source";

    private final CurrencyHarvester currencyHarvester;

    private final CurrencyRepository currencyRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public CurrencyService(
            CurrencyHarvester currencyHarvester,
            CurrencyRepository currencyRepository,
            ReferenceDataServiceSupport support) {
        this.currencyHarvester = currencyHarvester;
        this.currencyRepository = currencyRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(currencyRepository);
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
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(currencyHarvester, currencyRepository, dbSourceID, "currencies");
    }
}
