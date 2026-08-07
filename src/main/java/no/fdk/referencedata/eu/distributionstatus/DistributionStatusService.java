package no.fdk.referencedata.eu.distributionstatus;

import no.fdk.referencedata.core.ReferenceDataWriter;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DistributionStatusService {
    private final String dbSourceID = "distribution-statuses-source";

    private final DistributionStatusHarvester distributionStatusHarvester;

    private final ReferenceDataWriter referenceDataWriter;

    private final DistributionStatusRepository distributionStatusRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public DistributionStatusService(
            DistributionStatusHarvester distributionStatusHarvester,
            DistributionStatusRepository distributionStatusRepository,
            RDFSourceRepository rdfSourceRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.distributionStatusHarvester = distributionStatusHarvester;
        this.distributionStatusRepository = distributionStatusRepository;
        this.referenceDataWriter = referenceDataWriter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return distributionStatusRepository.count() == 0;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(dbSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    public void harvestAndSave() {
        try {

            final List<DistributionStatus> items = new ArrayList<>();
            distributionStatusHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} distribution statuses", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(distributionStatusHarvester.getModel(), RDFFormat.TURTLE));


            referenceDataWriter.replaceAll(distributionStatusRepository, items, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest distribution statuses", e);
        }
    }
}
