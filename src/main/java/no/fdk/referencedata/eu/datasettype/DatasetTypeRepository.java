package no.fdk.referencedata.eu.datasettype;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DatasetTypeRepository extends CrudRepository<DatasetType, String> {
    Optional<DatasetType> findByCode(String code);
}
