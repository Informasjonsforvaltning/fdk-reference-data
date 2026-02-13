package no.fdk.referencedata.eu.highvaluecategories;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface HighValueCategoryRepository extends CrudRepository<HighValueCategory, String> {
    Optional<HighValueCategory> findByCode(String code);
}
