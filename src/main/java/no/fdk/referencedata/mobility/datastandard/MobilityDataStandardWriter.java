package no.fdk.referencedata.mobility.datastandard;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class MobilityDataStandardWriter {

    private final MobilityDataStandardRepository mobilityDataStandardRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MobilityDataStandardWriter(
            MobilityDataStandardRepository mobilityDataStandardRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.mobilityDataStandardRepository = mobilityDataStandardRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<MobilityDataStandard> items, RDFSource rdfSource) {
        mobilityDataStandardRepository.deleteAll();
        mobilityDataStandardRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
