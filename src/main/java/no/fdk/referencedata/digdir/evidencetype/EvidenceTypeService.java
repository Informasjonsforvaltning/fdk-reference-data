package no.fdk.referencedata.digdir.evidencetype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import no.fdk.referencedata.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class EvidenceTypeService {

    private final EvidenceTypeHarvester evidenceTypeHarvester;

    private final EvidenceTypeRepository evidenceTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public EvidenceTypeService(EvidenceTypeHarvester evidenceTypeHarvester,
                               EvidenceTypeRepository evidenceTypeRepository,
                               HarvestSettingsRepository harvestSettingsRepository) {
        this.evidenceTypeHarvester = evidenceTypeHarvester;
        this.evidenceTypeRepository = evidenceTypeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return evidenceTypeRepository.count() == 0;
    }

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(evidenceTypeHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.EVIDENCE_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.EVIDENCE_TYPE.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                evidenceTypeRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<EvidenceType> iterable = evidenceTypeHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} evidence-types", counter.get());
                evidenceTypeRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(evidenceTypeHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest evidence-types", e);
        }
    }
}
