package no.fdk.referencedata.lovdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalResourceRepository extends JpaRepository<LegalResource, String> {
}
