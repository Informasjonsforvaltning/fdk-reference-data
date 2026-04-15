package no.fdk.referencedata.rdf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RDFSourceRepository extends JpaRepository<RDFSource, String> {}
