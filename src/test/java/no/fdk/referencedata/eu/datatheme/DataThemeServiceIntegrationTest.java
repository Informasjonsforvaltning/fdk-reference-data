package no.fdk.referencedata.eu.datatheme;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.LocalHarvestFixtures.DATA_THEMES_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class DataThemeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private DataThemeRepository dataThemeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_datathemes() {
        DataThemeService fileTypeService = new DataThemeService(
                LocalHarvesters.dataTheme(),
                dataThemeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        fileTypeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        dataThemeRepository.findAll().forEach(fileType -> counter.incrementAndGet());
        assertEquals(DATA_THEMES_SIZE, counter.get());

        final DataTheme first = dataThemeRepository.findById("http://publications.europa.eu/resource/authority/data-theme/AGRI").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/AGRI", first.getUri());
        assertEquals("AGRI", first.getCode());
        assertEquals("Agriculture, fisheries, forestry and food", first.getLabel().get(Language.ENGLISH.code()));
        assertEquals("http://publications.europa.eu/resource/authority/data-theme", first.getConceptSchema().getUri());
        assertEquals("Data theme", first.getConceptSchema().getLabel().get(Language.ENGLISH.code()));
        assertEquals("20220715-0", first.getConceptSchema().getVersionNumber());
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        DataThemeRepository dataThemeRepositorySpy = spy(this.dataThemeRepository);

        dataThemeRepositorySpy.save(DataTheme.builder()
                .uri("http://uri.no")
                .code("THEME")
                .label(Map.of("en", "My theme"))
                .build());

        long count = dataThemeRepositorySpy.count();
        assertTrue(count > 0);

        when(dataThemeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new DataThemeService(
                LocalHarvesters.dataTheme(),
                dataThemeRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        assertEquals(count, dataThemeRepositorySpy.count());
    }
}
