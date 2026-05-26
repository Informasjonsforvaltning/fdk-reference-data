package no.fdk.referencedata.adms.status;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ADMSStatusWriter {

    private final ADMSStatusRepository admsStatusRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ADMSStatusWriter(ADMSStatusRepository admsStatusRepository, RDFSourceRepository rdfSourceRepository) {
        this.admsStatusRepository = admsStatusRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<ADMSStatus> items, RDFSource rdfSource) {
        admsStatusRepository.deleteAll();
        admsStatusRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
