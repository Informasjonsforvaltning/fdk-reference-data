package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.currency.Currency;
import no.fdk.referencedata.eu.currency.CurrencyRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CurrencyQuery {

    private final CurrencyRepository currencyRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<Currency> currencies() {
        return support.allSortedByUri(currencyRepository, Currency::getUri);
    }

    @QueryMapping
    public Currency currencyByCode(@Argument String code) {
        return support.byCode(currencyRepository::findByCode, code);
    }
}
