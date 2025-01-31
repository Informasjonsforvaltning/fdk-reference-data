package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.currency.Currency;
import no.fdk.referencedata.eu.currency.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class CurrencyQuery {

    @Autowired
    private CurrencyRepository currencyRepository;

    @QueryMapping
    public List<Currency> currencies() {
        return StreamSupport.stream(currencyRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(Currency::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public Currency currencyByCode(@Argument String code) {
        return currencyRepository.findByCode(code).orElse(null);
    }
}
