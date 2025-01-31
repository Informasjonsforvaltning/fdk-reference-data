package no.fdk.referencedata.eu.currency;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.eu.currency.LocalCurrencyHarvester.CURRENCY_SIZE;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
public class CurrencyHarvesterTest {

    @Test
    public void test_fetch_currencies() {
        CurrencyHarvester harvester = new LocalCurrencyHarvester("20241211-0");

        assertNotNull(harvester.getSource());
        assertEquals("currency-sparql-result.ttl", harvester.getSource().getFilename());
        assertEquals("20241211-0", harvester.getVersion());

        List<Currency> currencies = harvester.harvest().collectList().block();
        assertNotNull(currencies);
        assertEquals(CURRENCY_SIZE, currencies.size());

        Currency nok = currencies.stream().filter(c -> c.code.equals("NOK")).findFirst().get();
        assertEquals("http://publications.europa.eu/resource/authority/currency/NOK", nok.getUri());
        assertEquals("NOK", nok.getCode());
        assertEquals("Norwegian krone", nok.getLabel().get(Language.ENGLISH.code()));
        assertEquals("Norsk krone", nok.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals("Norsk krone", nok.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));

        Currency cup = currencies.stream().filter(c -> c.code.equals("CUP")).findFirst().get();
        assertEquals("http://publications.europa.eu/resource/authority/currency/CUP", cup.getUri());
        assertEquals("CUP", cup.getCode());
        assertEquals("Cuban peso", cup.getLabel().get(Language.ENGLISH.code()));
        assertNull(cup.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertNull(cup.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));

        Currency gbp = currencies.stream().filter(c -> c.code.equals("GBP")).findFirst().get();
        assertEquals("http://publications.europa.eu/resource/authority/currency/GBP", gbp.getUri());
        assertEquals("GBP", gbp.getCode());
        assertEquals("Pound sterling", gbp.getLabel().get(Language.ENGLISH.code()));
        assertEquals("Britisk pund", gbp.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals("Britisk pund", gbp.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
    }

}
