package no.fdk.referencedata.core;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ReferenceDataWriter {

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ReferenceDataWriter(RDFSourceRepository rdfSourceRepository) {
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public <T> void replaceAll(JpaRepository<T, String> repository, List<T> items, RDFSource rdfSource) {
        repository.deleteAll();
        repository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }

    @Transactional
    public <T> void replaceAll(JpaRepository<T, String> repository, List<T> items) {
        repository.deleteAll();
        repository.saveAll(items);
    }
}
