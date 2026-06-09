package no.fdk.referencedata.digdir.relationshipwithsourcetype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class RelationshipWithSourceTypeWriter {

    private final RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public RelationshipWithSourceTypeWriter(
            RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.relationshipWithSourceTypeRepository = relationshipWithSourceTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<RelationshipWithSourceType> items, RDFSource rdfSource) {
        relationshipWithSourceTypeRepository.deleteAll();
        relationshipWithSourceTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
