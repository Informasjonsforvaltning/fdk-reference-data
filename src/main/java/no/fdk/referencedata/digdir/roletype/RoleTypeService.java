package no.fdk.referencedata.digdir.roletype;

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
public class RoleTypeService {

    private final RoleTypeHarvester roleTypeHarvester;

    private final RoleTypeRepository roleTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public RoleTypeService(RoleTypeHarvester roleTypeHarvester,
                           RoleTypeRepository roleTypeRepository,
                           HarvestSettingsRepository harvestSettingsRepository) {
        this.roleTypeHarvester = roleTypeHarvester;
        this.roleTypeRepository = roleTypeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return roleTypeRepository.count() == 0;
    }

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(roleTypeHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.ROLE_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.ROLE_TYPE.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                roleTypeRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<RoleType> iterable = roleTypeHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} role-types", counter.get());
                roleTypeRepository.saveAll(iterable);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(roleTypeHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest role-types", e);
        }
    }
}
