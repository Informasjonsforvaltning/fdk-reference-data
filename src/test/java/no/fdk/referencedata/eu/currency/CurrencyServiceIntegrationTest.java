package no.fdk.referencedata.eu.currency;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.LocalHarvestFixtures.CURRENCY_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class CurrencyServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_currencies() {
        CurrencyService currencyService = new CurrencyService(
                LocalHarvesters.currency(),
                currencyRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        currencyService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        currencyRepository.findAll().forEach(status -> counter.incrementAndGet());
        assertEquals(CURRENCY_SIZE, counter.get());

        final Currency first = currencyRepository.findById("http://publications.europa.eu/resource/authority/currency/DKK").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/currency/DKK", first.getUri());
        assertEquals("DKK", first.getCode());
        assertEquals("Dansk krone", first.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
    }

    @Test
    public void test_if_harvest_rolls_back_transaction_when_save_fails() {
        CurrencyRepository currencyRepositorySpy = spy(this.currencyRepository);

        Currency currency = Currency.builder()
                .uri("http://uri.no")
                .code("CURRENCY_A")
                .label(Map.of("en", "My currency"))
                .build();
        currencyRepositorySpy.save(currency);

        long count = currencyRepositorySpy.count();
        assertTrue(count > 0);

        when(currencyRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new CurrencyService(
                LocalHarvesters.currency(),
                currencyRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        assertEquals(count, currencyRepositorySpy.count());
    }
}
