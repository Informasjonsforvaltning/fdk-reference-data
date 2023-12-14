package no.fdk.referencedata.digdir.roletype;

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

import static no.fdk.referencedata.settings.Settings.ROLE_TYPE;
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

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_datathemes() {
        RoleTypeService roleTypeService = new RoleTypeService(
                new LocalRoleTypeHarvester("123-0"),
                roleTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        roleTypeService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        roleTypeRepository.findAll().forEach(roleType -> counter.incrementAndGet());
        assertEquals(5, counter.get());

        final RoleType first = roleTypeRepository.findById("https://data.norge.no/vocabulary/role-type#service-receiver").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/role-type#service-receiver", first.getUri());
        assertEquals("service-receiver", first.getCode());
        assertEquals("service receiver", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        RoleTypeService roleTypeService = new RoleTypeService(
                new LocalRoleTypeHarvester("132-0"),
                roleTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        roleTypeService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(ROLE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        roleTypeService = new RoleTypeService(
                new LocalRoleTypeHarvester("132-2"),
                roleTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        roleTypeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(ROLE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        roleTypeService = new RoleTypeService(
                new LocalRoleTypeHarvester("132-1"),
                roleTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        roleTypeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(ROLE_TYPE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("132-2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
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

        RoleTypeService roleTypeService = new RoleTypeService(
                new LocalRoleTypeHarvester("123-2"),
                roleTypeRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, roleTypeRepositorySpy.count());
    }
}
