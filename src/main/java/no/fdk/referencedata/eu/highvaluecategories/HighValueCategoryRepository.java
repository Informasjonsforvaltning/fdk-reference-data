package no.fdk.referencedata.eu.highvaluecategories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HighValueCategoryRepository extends CrudRepository<HighValueCategory, String> {
    Optional<HighValueCategory> findByCode(String code);
}
