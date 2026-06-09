package no.fdk.referencedata.digdir.audiencetype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AudienceTypeWriter {

    private final AudienceTypeRepository audienceTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public AudienceTypeWriter(
            AudienceTypeRepository audienceTypeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.audienceTypeRepository = audienceTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<AudienceType> items, RDFSource rdfSource) {
        audienceTypeRepository.deleteAll();
        audienceTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
