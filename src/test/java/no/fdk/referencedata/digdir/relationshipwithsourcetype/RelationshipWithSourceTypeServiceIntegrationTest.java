package no.fdk.referencedata.digdir.relationshipwithsourcetype;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class RelationshipWithSourceTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_relationshipWithSource_types() {
        RelationshipWithSourceTypeService relationshipWithSourceTypeService = new RelationshipWithSourceTypeService(
                LocalHarvesters.relationshipWithSourceType(),
                relationshipWithSourceTypeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        relationshipWithSourceTypeService.harvestAndSave();

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

        new RelationshipWithSourceTypeService(
                LocalHarvesters.relationshipWithSourceType(),
                relationshipWithSourceTypeRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        assertEquals(count, relationshipWithSourceTypeRepositorySpy.count());
    }
}
