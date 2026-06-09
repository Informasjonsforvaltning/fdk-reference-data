package no.fdk.referencedata.digdir.roletype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class RoleTypeWriter {

    private final RoleTypeRepository roleTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public RoleTypeWriter(
            RoleTypeRepository roleTypeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.roleTypeRepository = roleTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<RoleType> items, RDFSource rdfSource) {
        roleTypeRepository.deleteAll();
        roleTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
