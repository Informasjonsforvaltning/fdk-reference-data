package no.fdk.referencedata.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HarvestSettingsRepository extends JpaRepository<HarvestSettings, String> {}
