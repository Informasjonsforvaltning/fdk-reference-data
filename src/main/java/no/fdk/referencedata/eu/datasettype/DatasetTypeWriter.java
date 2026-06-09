package no.fdk.referencedata.eu.datasettype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DatasetTypeWriter {

    private final DatasetTypeRepository datasetTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public DatasetTypeWriter(
            DatasetTypeRepository datasetTypeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.datasetTypeRepository = datasetTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<DatasetType> items, RDFSource rdfSource) {
        datasetTypeRepository.deleteAll();
        datasetTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
