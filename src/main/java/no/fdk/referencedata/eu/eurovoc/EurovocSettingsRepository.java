package no.fdk.referencedata.eu.eurovoc;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EurovocSettingsRepository extends CrudRepository<EurovocSettings, String> {}
