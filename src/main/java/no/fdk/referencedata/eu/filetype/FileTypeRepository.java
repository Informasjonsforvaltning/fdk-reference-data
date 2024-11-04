package no.fdk.referencedata.eu.filetype;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface FileTypeRepository extends CrudRepository<FileType, String> {
    Stream<FileType> findByUriIn(List<String> uris);
    Stream<FileType> findByCodeContainingIgnoreCase(String query);
    Optional<FileType> findByCode(String code);
}
