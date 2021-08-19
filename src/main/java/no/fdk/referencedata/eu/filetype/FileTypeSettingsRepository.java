package no.fdk.referencedata.eu.filetype;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileTypeSettingsRepository extends CrudRepository<FileTypeSettings, String> {}
