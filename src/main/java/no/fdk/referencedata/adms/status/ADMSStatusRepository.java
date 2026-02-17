package no.fdk.referencedata.adms.status;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ADMSStatusRepository extends ListCrudRepository<ADMSStatus, String> {
    Optional<ADMSStatus> findByCode(String code);
}
