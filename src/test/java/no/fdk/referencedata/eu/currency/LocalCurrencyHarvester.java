package no.fdk.referencedata.eu.currency;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalCurrencyHarvester extends CurrencyHarvester {
    private final String version;
    public static final int CURRENCY_SIZE = 14;

    public LocalCurrencyHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("currency-sparql-result.ttl");
    }
}
