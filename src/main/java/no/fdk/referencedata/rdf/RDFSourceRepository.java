package no.fdk.referencedata.rdf;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RDFSourceRepository extends CrudRepository<RDFSource, String> {}
