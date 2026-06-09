package no.fdk.referencedata.mobility.theme;

import no.fdk.referencedata.mobility.theme.MobilityThemeWriter;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class MobilityThemeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MobilityThemeRepository mobilityThemeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_themes() {
        MobilityThemeService mobilityThemeService = new MobilityThemeService(
                new LocalMobilityThemeHarvester(),
                mobilityThemeRepository,
                rdfSourceRepository,
                new MobilityThemeWriter(mobilityThemeRepository, rdfSourceRepository));

        mobilityThemeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        mobilityThemeRepository.findAll().forEach(theme -> counter.incrementAndGet());
        assertEquals(123, counter.get());

        final MobilityTheme first = mobilityThemeRepository.findById("https://w3id.org/mobilitydcat-ap/mobility-theme/vehicle-details").orElseThrow();
        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-theme/vehicle-details", first.getUri());
        assertEquals("vehicle-details", first.getCode());
        assertEquals("Vehicle details", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        MobilityThemeRepository mobilityThemeRepositorySpy = spy(this.mobilityThemeRepository);

        MobilityTheme theme = MobilityTheme.builder()
                .uri("http://uri.no")
                .code("MOBILITY_THEME")
                .label(Map.of("en", "My theme"))
                .build();
        mobilityThemeRepositorySpy.save(theme);

        long count = mobilityThemeRepositorySpy.count();
        assertTrue(count > 0);

        when(mobilityThemeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new MobilityThemeService(
                new LocalMobilityThemeHarvester(),
                mobilityThemeRepositorySpy,
                rdfSourceRepository,
                new MobilityThemeWriter(mobilityThemeRepository, rdfSourceRepository));

        assertEquals(count, mobilityThemeRepositorySpy.count());
    }
}
