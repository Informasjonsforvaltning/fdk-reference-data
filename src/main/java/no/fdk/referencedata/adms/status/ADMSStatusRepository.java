package no.fdk.referencedata.adms.status;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface ADMSStatusRepository extends ListCrudRepository<ADMSStatus, String> {
    Optional<ADMSStatus> findByCode(String code);
}
