package no.fdk.referencedata.eu.datasettype;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatasetTypeRepository extends CrudRepository<DatasetType, String> {
    Optional<DatasetType> findByCode(String code);
}
