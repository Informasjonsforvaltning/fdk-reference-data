package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class KommuneService {

    private final KommuneHarvester kommuneHarvester;

    private final KommuneRepository kommuneRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public KommuneService(KommuneHarvester kommuneHarvester,
                          KommuneRepository kommuneRepository,
                          HarvestSettingsRepository harvestSettingsRepository) {
        this.kommuneHarvester = kommuneHarvester;
        this.kommuneRepository = kommuneRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return kommuneRepository.count() == 0;
    }

    @Transactional
    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.GEONORGE_KOMMUNE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.GEONORGE_KOMMUNE.name())
                            .latestVersion("0")
                            .build());

            kommuneRepository.deleteAll();

            final AtomicInteger counter = new AtomicInteger(0);
            final Iterable<Kommune> iterable = kommuneHarvester.harvest().toIterable();
            iterable.forEach(item -> counter.getAndIncrement());
            log.info("Harvest and saving {} GeoNorge kommuner", counter.get());
            kommuneRepository.saveAll(iterable);

            settings.setLatestHarvestDate(LocalDateTime.now());
            harvestSettingsRepository.save(settings);
        } catch(Exception e) {
            log.error("Unable to harvest GeoNorge kommuner", e);
        }
    }
}
