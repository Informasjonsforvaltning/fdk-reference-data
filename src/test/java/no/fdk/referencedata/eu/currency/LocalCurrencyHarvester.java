package no.fdk.referencedata.eu.currency;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalCurrencyHarvester extends CurrencyHarvester {
    public static final int CURRENCY_SIZE = 14;

    @Override
    public Resource getSource() {
        return new ClassPathResource("currency-sparql-result.ttl");
    }
}
