package no.fdk.referencedata.eu.licence;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
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

import static no.fdk.referencedata.eu.licence.LocalLicenceHarvester.LICENCES_SIZE;
import static no.fdk.referencedata.settings.Settings.LICENCE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class LicenceServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private LicenceRepository licenceRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_licences() {
        LicenceService licenceService = new LicenceService(
                new LocalLicenceHarvester("20240610-0"),
                licenceRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        licenceService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        licenceRepository.findAll().forEach(licence -> counter.incrementAndGet());
        assertEquals(LICENCES_SIZE, counter.get());

        final Licence first = licenceRepository.findById("http://publications.europa.eu/resource/authority/licence/CC0").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/licence/CC0", first.getUri());
        assertEquals("CC0", first.getCode());
        assertEquals("Creative Commons CC0 1.0 Universal", first.getLabel().get(Language.ENGLISH.code()));
        assertEquals(false, first.deprecated);
        assertEquals("The person who associated a work with CC0 1.0 has dedicated the work to the public domain by waiving all of his or her rights to the work worldwide under copyright law, including all related and neighboring rights, to the extent allowed by law. One can copy, modify, distribute and perform the work, even for commercial purposes, all without asking permission.", first.definition.get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        LicenceService licenceService = new LicenceService(
                new LocalLicenceHarvester("20240610-1"),
                licenceRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        licenceService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(LICENCE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20240610-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        licenceService = new LicenceService(
                new LocalLicenceHarvester("20240611-0"),
                licenceRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        licenceService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(LICENCE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20240611-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        licenceService = new LicenceService(
                new LocalLicenceHarvester("20240611-0"),
                licenceRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        licenceService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(LICENCE.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20240611-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        LicenceRepository licenceRepositorySpy = spy(this.licenceRepository);

        Licence licence = Licence.builder()
                .uri("http://uri.no")
                .code("LICENCE")
                .label(Map.of("en", "My licence"))
                .build();
        licenceRepositorySpy.save(licence);

        long count = licenceRepositorySpy.count();
        assertTrue(count > 0);

        when(licenceRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        LicenceService licenceService = new LicenceService(
                new LocalLicenceHarvester("20240611-2"),
                licenceRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, licenceRepositorySpy.count());
    }
} 
