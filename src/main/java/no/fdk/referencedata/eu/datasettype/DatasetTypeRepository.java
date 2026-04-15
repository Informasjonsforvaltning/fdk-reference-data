package no.fdk.referencedata.eu.datasettype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatasetTypeRepository extends JpaRepository<DatasetType, String> {
    Optional<DatasetType> findByCode(String code);
}
