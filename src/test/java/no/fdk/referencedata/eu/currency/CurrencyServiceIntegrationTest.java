package no.fdk.referencedata.eu.currency;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.eu.currency.LocalCurrencyHarvester.CURRENCY_SIZE;
import static no.fdk.referencedata.settings.Settings.CURRENCY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class CurrencyServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_currencies() {
        CurrencyService currencyService = new CurrencyService(
                new LocalCurrencyHarvester("20220715-0"),
                currencyRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        currencyService.harvestAndSave(true);

        final AtomicInteger counter = new AtomicInteger();
        currencyRepository.findAll().forEach(status -> counter.incrementAndGet());
        assertEquals(CURRENCY_SIZE, counter.get());

        final Currency first = currencyRepository.findById("http://publications.europa.eu/resource/authority/currency/DKK").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/currency/DKK", first.getUri());
        assertEquals("DKK", first.getCode());
        assertEquals("Dansk krone", first.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        CurrencyService currencyService = new CurrencyService(
                new LocalCurrencyHarvester("2"),
                currencyRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        currencyService.harvestAndSave(true);

        HarvestSettings settings =
                harvestSettingsRepository.findById(CURRENCY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        currencyService = new CurrencyService(
                new LocalCurrencyHarvester("20220715-1"),
                currencyRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        currencyService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(CURRENCY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20220715-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        currencyService = new CurrencyService(
                new LocalCurrencyHarvester("20210715-0"),
                currencyRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        currencyService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(CURRENCY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20220715-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
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

        CurrencyService currencyService = new CurrencyService(
                new LocalCurrencyHarvester("20200924-2"),
                currencyRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, currencyRepositorySpy.count());
    }
}
