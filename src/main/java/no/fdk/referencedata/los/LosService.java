package no.fdk.referencedata.los;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.core.HarvestMetrics;
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
    private static final String MODULE_ID = "los";

    private final String rdfSourceID = "los-source";
    private final LosRepository losRepository;
    private final ReferenceDataServiceSupport support;
    private final HarvestMetrics harvestMetrics;
    public LosImporter losImporter;

    @Autowired
    public LosService(
            LosImporter losImporter,
            LosRepository losRepository,
            ReferenceDataServiceSupport support,
            HarvestMetrics harvestMetrics) {
        this.losImporter = losImporter;
        this.losRepository = losRepository;
        this.support = support;
        this.harvestMetrics = harvestMetrics;
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
        return harvestMetrics.timed(MODULE_ID, () -> {
            try {
                final List<LosNode> losList = losImporter.importFromLosSource();

                if (losList.isEmpty()) {
                    log.warn("Harvest for {} returned no items; skipping replace", MODULE_ID);
                    return HarvestResult.skippedEmpty();
                }

                log.info("Harvest and saving {} {}", losList.size(), MODULE_ID);
                support.saveAll(losList, losImporter.getModel(), losRepository, rdfSourceID);
                return HarvestResult.success(losList.size());
            } catch (Exception e) {
                log.error("Unable to harvest {}", MODULE_ID, e);
                return HarvestResult.failure();
            }
        });
    }

    @Override
    public HarvestResult harvestAndSave() {
        return importLosNodes();
    }
}
