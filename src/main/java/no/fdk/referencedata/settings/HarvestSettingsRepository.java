package no.fdk.referencedata.settings;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HarvestSettingsRepository extends CrudRepository<HarvestSettings, String> {}
