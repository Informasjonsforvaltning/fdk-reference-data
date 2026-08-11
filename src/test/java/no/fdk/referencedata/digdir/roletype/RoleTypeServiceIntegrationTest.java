package no.fdk.referencedata.digdir.roletype;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.HarvestMetrics;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class RoleTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private RoleTypeRepository roleTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_datathemes() {
        RoleTypeService roleTypeService = new RoleTypeService(
                LocalHarvesters.roleType(),
                roleTypeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        roleTypeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        roleTypeRepository.findAll().forEach(roleType -> counter.incrementAndGet());
        assertEquals(5, counter.get());

        final RoleType first = roleTypeRepository.findById("https://data.norge.no/vocabulary/role-type#service-receiver").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/role-type#service-receiver", first.getUri());
        assertEquals("service-receiver", first.getCode());
        assertEquals("service receiver", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        RoleTypeRepository roleTypeRepositorySpy = spy(this.roleTypeRepository);

        RoleType roleType = RoleType.builder()
                .uri("http://uri.no")
                .code("ROLE_TYPE")
                .label(Map.of("en", "My role"))
                .build();
        roleTypeRepositorySpy.save(roleType);

        long count = roleTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(roleTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new RoleTypeService(
                LocalHarvesters.roleType(),
                roleTypeRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        assertEquals(count, roleTypeRepositorySpy.count());
    }
}
