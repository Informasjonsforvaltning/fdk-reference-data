package no.fdk.referencedata.los;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class LosService implements HarvestableReferenceData {
    private final String rdfSourceID = "los-source";
    private final LosRepository losRepository;
    private final ReferenceDataServiceSupport support;
    public LosImporter losImporter;

    @Autowired
    public LosService(
            LosImporter losImporter,
            LosRepository losRepository,
            ReferenceDataServiceSupport support) {
        this.losImporter = losImporter;
        this.losRepository = losRepository;
        this.support = support;
    }

    public List<LosNode> getByURIs(List<String> uris) {
        return losRepository.findByUriIn(uris).stream()
                .sorted(Comparator.comparing(LosNode::getUri))
                .toList();
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(rdfSourceID, rdfFormat);
    }

    public List<LosNode> getAll() {
        return losRepository.findAll().stream()
                .sorted(Comparator.comparing(LosNode::getUri))
                .toList();
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(losRepository);
    }

    public HarvestResult importLosNodes() {
        final List<LosNode> losList = losImporter.importFromLosSource();
        return support.persistHarvested("los", losList, losImporter.getModel(), losRepository, rdfSourceID);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return importLosNodes();
    }
}
