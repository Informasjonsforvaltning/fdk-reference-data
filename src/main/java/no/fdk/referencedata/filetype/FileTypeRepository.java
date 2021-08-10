package no.fdk.referencedata.filetype;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FileTypeRepository extends CrudRepository<FileType, String> {
    Optional<FileType> findByCode(String code);
}
