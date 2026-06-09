package no.fdk.referencedata.eu.filetype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class FileTypeWriter {

    private final FileTypeRepository fileTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public FileTypeWriter(
            FileTypeRepository fileTypeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.fileTypeRepository = fileTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<FileType> items, RDFSource rdfSource) {
        fileTypeRepository.deleteAll();
        fileTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
