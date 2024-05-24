package no.fdk.referencedata.digdir.relationshipwithsourcetype;

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

import static no.fdk.referencedata.settings.Settings.RELATIONSHIP_WITH_SOURCE_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class RelationshipWithSourceTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_relationshipWithSource_types() {
        RelationshipWithSourceTypeService relationshipWithSourceTypeService = new RelationshipWithSourceTypeService(
                new LocalRelationshipWithSourceTypeHarvester("123-0"),
                relationshipWithSourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        relationshipWithSourceTypeService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        relationshipWithSourceTypeRepository.findAll().forEach(relationshipWithSourceType -> counter.incrementAndGet());
        assertEquals(3, counter.get());

        final RelationshipWithSourceType first = relationshipWithSourceTypeRepository.findById("https://data.norge.no/vocabulary/relationship-with-source-type#self-composed").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/relationship-with-source-type#self-composed", first.getUri());
        assertEquals("self-composed", first.getCode());
        assertEquals("self-composed", first.getLabel().get(Language.ENGLISH.code()));
        assertEquals("eigendefinert", first.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        RelationshipWithSourceTypeService relationshipWithSourceTypeService = new RelationshipWithSourceTypeService(
                new LocalRelationshipWithSourceTypeHarvester("132-0"),
                relationshipWithSourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        relationshipWithSourceTypeService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(RELATIONSHIP_WITH_SOURCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        relationshipWithSourceTypeService = new RelationshipWithSourceTypeService(
                new LocalRelationshipWithSourceTypeHarvester("132-2"),
                relationshipWithSourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        relationshipWithSourceTypeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(RELATIONSHIP_WITH_SOURCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        relationshipWithSourceTypeService = new RelationshipWithSourceTypeService(
                new LocalRelationshipWithSourceTypeHarvester("132-1"),
                relationshipWithSourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        relationshipWithSourceTypeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(RELATIONSHIP_WITH_SOURCE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepositorySpy = spy(this.relationshipWithSourceTypeRepository);

        RelationshipWithSourceType relationshipWithSourceType = RelationshipWithSourceType.builder()
                .uri("http://uri.no")
                .code("RELATIONSHIP_WITH_SOURCE_TYPE")
                .label(Map.of("en", "My relationshipWithSource"))
                .build();
        relationshipWithSourceTypeRepositorySpy.save(relationshipWithSourceType);


        long count = relationshipWithSourceTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(relationshipWithSourceTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        RelationshipWithSourceTypeService relationshipWithSourceTypeService = new RelationshipWithSourceTypeService(
                new LocalRelationshipWithSourceTypeHarvester("123-2"),
                relationshipWithSourceTypeRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, relationshipWithSourceTypeRepositorySpy.count());
    }
}
