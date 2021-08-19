package no.fdk.referencedata.eu.datatheme;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataThemeSettingsRepository extends CrudRepository<DataThemeSettings, String> {}
