package no.fdk.referencedata.adms.status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ADMSStatusRepository extends JpaRepository<ADMSStatus, String> {
    Optional<ADMSStatus> findByCode(String code);
}
