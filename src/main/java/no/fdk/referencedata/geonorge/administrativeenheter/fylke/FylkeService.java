package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneRepository;
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
public class FylkeService {

    private final FylkeHarvester fylkeHarvester;

    private final FylkeRepository fylkeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public FylkeService(FylkeHarvester fylkeHarvester,
                        FylkeRepository fylkeRepository,
                        HarvestSettingsRepository harvestSettingsRepository) {
        this.fylkeHarvester = fylkeHarvester;
        this.fylkeRepository = fylkeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return fylkeRepository.count() == 0;
    }

    @Transactional
    public void harvestAndSave() {
        try {
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.GEONORGE_FYLKE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.GEONORGE_FYLKE.name())
                            .latestVersion("0")
                            .build());

            fylkeRepository.deleteAll();

            final AtomicInteger counter = new AtomicInteger(0);
            final Iterable<Fylke> iterable = fylkeHarvester.harvest().toIterable();
            iterable.forEach(item -> counter.getAndIncrement());
            log.info("Harvest and saving {} GeoNorge fylker", counter.get());
            fylkeRepository.saveAll(iterable);

            settings.setLatestHarvestDate(LocalDateTime.now());
            harvestSettingsRepository.save(settings);
        } catch(Exception e) {
            log.error("Unable to harvest GeoNorge fylker", e);
        }
    }
}
