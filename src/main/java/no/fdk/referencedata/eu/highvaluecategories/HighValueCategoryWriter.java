package no.fdk.referencedata.eu.highvaluecategories;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class HighValueCategoryWriter {

    private final HighValueCategoryRepository highValueCategoryRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public HighValueCategoryWriter(
            HighValueCategoryRepository highValueCategoryRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.highValueCategoryRepository = highValueCategoryRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<HighValueCategory> items, RDFSource rdfSource) {
        highValueCategoryRepository.deleteAll();
        highValueCategoryRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
