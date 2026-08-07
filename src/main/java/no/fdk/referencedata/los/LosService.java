package no.fdk.referencedata.los;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.core.HarvestableReferenceData;
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

    public void importLosNodes() {
        try {
            final List<LosNode> losList = losImporter.importFromLosSource();
            support.saveAll(losList, losImporter.getModel(), losRepository, rdfSourceID);
        } catch (Exception e) {
            log.error("Unable to harvest LOS", e);
        }
    }

    @Override
    public void harvestAndSave() {
        importLosNodes();
    }
}
