package no.fdk.referencedata.lovdata;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.core.HarvestMetrics;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LovdataService implements HarvestableReferenceData {

    private static final String MODULE_ID = "legal-resource";
    private static final String SOURCE_ID = "legal-resources-source";

    private final LovdataHarvester lovdataHarvester;
    private final LegalResourceRepository legalResourceRepository;
    private final ReferenceDataServiceSupport support;
    private final HarvestMetrics harvestMetrics;

    @Autowired
    public LovdataService(
            LovdataHarvester lovdataHarvester,
            LegalResourceRepository legalResourceRepository,
            ReferenceDataServiceSupport support,
            HarvestMetrics harvestMetrics) {
        this.lovdataHarvester = lovdataHarvester;
        this.legalResourceRepository = legalResourceRepository;
        this.support = support;
        this.harvestMetrics = harvestMetrics;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(legalResourceRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(SOURCE_ID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return harvestMetrics.timed(MODULE_ID, () -> {
            try {
                final List<LegalResource> legalResources = new ArrayList<>();
                lovdataHarvester.harvest().toIterable().forEach(legalResources::add);

                if (legalResources.isEmpty()) {
                    log.warn("Harvest for {} returned no items; skipping replace", MODULE_ID);
                    return HarvestResult.skippedEmpty();
                }

                log.info("Harvest and saving {} {}", legalResources.size(), MODULE_ID);
                support.saveAll(
                        legalResources,
                        LegalResourceModel.from(legalResources),
                        legalResourceRepository,
                        SOURCE_ID);
                return HarvestResult.success(legalResources.size());
            } catch (Exception e) {
                log.error("Unable to harvest {}", MODULE_ID, e);
                return HarvestResult.fromThrowable(e);
            }
        });
    }
}
