package no.fdk.referencedata.eu.language;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.LocalHarvestFixtures.LANGUAGES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class LanguageServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private LanguageRepository languageRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_languages() {
        LanguageService languageService = new LanguageService(
                LocalHarvesters.language(),
                languageRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        languageService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        languageRepository.findAll().forEach(language -> counter.incrementAndGet());
        assertEquals(LANGUAGES_SIZE, counter.get());

        final Language first = languageRepository
                .findById("http://publications.europa.eu/resource/authority/language/ENG")
                .orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/language/ENG", first.getUri());
        assertEquals("ENG", first.getCode());
        assertEquals("English", first.getLabel().get(no.fdk.referencedata.i18n.Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        LanguageRepository languageRepositorySpy = spy(this.languageRepository);

        Language language = Language.builder()
                .uri("http://uri.no")
                .code("ENG")
                .label(Map.of("en", "English"))
                .build();
        languageRepositorySpy.save(language);

        long count = languageRepositorySpy.count();
        assertTrue(count > 0);

        when(languageRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new LanguageService(
                LocalHarvesters.language(),
                languageRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        assertEquals(count, languageRepositorySpy.count());
    }
}
