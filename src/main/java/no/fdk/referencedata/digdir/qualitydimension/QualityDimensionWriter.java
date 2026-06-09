package no.fdk.referencedata.digdir.qualitydimension;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class QualityDimensionWriter {

    private final QualityDimensionRepository qualityDimensionRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public QualityDimensionWriter(
            QualityDimensionRepository qualityDimensionRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.qualityDimensionRepository = qualityDimensionRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<QualityDimension> items, RDFSource rdfSource) {
        qualityDimensionRepository.deleteAll();
        qualityDimensionRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
