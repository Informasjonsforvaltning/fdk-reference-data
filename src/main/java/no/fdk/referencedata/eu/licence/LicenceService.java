package no.fdk.referencedata.eu.licence;

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
public class LicenceService implements SearchableReferenceData, HarvestableReferenceData {
    private final String dbSourceID = "licences-source";

    private final LicenceHarvester licenceHarvester;

    private final LicenceRepository licenceRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public LicenceService(
            LicenceHarvester licenceHarvester,
            LicenceRepository licenceRepository,
            ReferenceDataServiceSupport support) {
        this.licenceHarvester = licenceHarvester;
        this.licenceRepository = licenceRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(licenceRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(licenceHarvester, licenceRepository, dbSourceID, "licences");
    }

    @Override
    public Stream<SearchHit> search(String query) {
        return Stream.empty();
    }

    @Override
    public Stream<SearchHit> findByURIs(List<String> uris) {
        return Stream.empty();
    }

    @Override
    public SearchAlternative getSearchType() {
        return null;
    }
}
