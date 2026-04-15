package no.fdk.referencedata.eu.filetype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileTypeRepository extends JpaRepository<FileType, String> {
    List<FileType> findByUriIn(List<String> uris);
    List<FileType> findByCodeContainingIgnoreCase(String query);
    Optional<FileType> findByCode(String code);
}
