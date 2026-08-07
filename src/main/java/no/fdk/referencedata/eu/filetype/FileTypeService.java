package no.fdk.referencedata.eu.filetype;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.search.SearchAlternative;
import no.fdk.referencedata.search.SearchHit;
import no.fdk.referencedata.search.SearchableReferenceData;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class FileTypeService implements SearchableReferenceData, HarvestableReferenceData {
    private final String dbSourceID = "file-types-source";

    private final FileTypeHarvester fileTypeHarvester;

    private final FileTypeRepository fileTypeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public FileTypeService(
            FileTypeHarvester fileTypeHarvester,
            FileTypeRepository fileTypeRepository,
            ReferenceDataServiceSupport support) {
        this.fileTypeHarvester = fileTypeHarvester;
        this.fileTypeRepository = fileTypeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(fileTypeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    public SearchAlternative getSearchType() {
        return SearchAlternative.EU_FILE_TYPES;
    }

    public Stream<SearchHit> search(String query) {
        return fileTypeRepository.findByCodeContainingIgnoreCase(query)
                .stream()
                .map(FileType::toSearchHit);
    }

    public Stream<SearchHit> findByURIs(List<String> uris) {
        return fileTypeRepository.findByUriIn(uris)
                .stream()
                .map(FileType::toSearchHit);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(fileTypeHarvester, fileTypeRepository, dbSourceID, "file-types");
    }
}
