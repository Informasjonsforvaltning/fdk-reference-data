package no.fdk.referencedata.eu.eurovoc;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.datatheme.DataTheme;
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
public class EuroVocService {

    private final EuroVocHarvester euroVocHarvester;

    private final EuroVocRepository euroVocRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public EuroVocService(EuroVocHarvester euroVocHarvester,
                          EuroVocRepository euroVocRepository,
                          HarvestSettingsRepository harvestSettingsRepository) {
        this.euroVocHarvester = euroVocHarvester;
        this.euroVocRepository = euroVocRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    @Transactional
    public void harvestAndSave() {
        try {
            final String version = euroVocHarvester.getVersion();
            final double versionDoubleValue = Double.parseDouble(euroVocHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.EURO_VOC.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.EURO_VOC.name())
                            .latestVersion("0")
                            .build());

            final double currentVersion = Double.parseDouble(settings.getLatestVersion().replace("-", ""));

            if(currentVersion < versionDoubleValue) {
                euroVocRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<EuroVoc> iterable = euroVocHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} eurovocs", counter.get());
                euroVocRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(version);
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest eurovoc", e);
        }
    }
}
