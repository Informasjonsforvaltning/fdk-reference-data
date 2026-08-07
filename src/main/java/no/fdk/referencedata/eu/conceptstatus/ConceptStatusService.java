package no.fdk.referencedata.eu.conceptstatus;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConceptStatusService implements HarvestableReferenceData {
    private final String dbSourceID = "concept-status-source";

    private final ConceptStatusHarvester conceptStatusHarvester;

    private final ConceptStatusRepository conceptStatusRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public ConceptStatusService(
            ConceptStatusHarvester conceptStatusHarvester,
            ConceptStatusRepository conceptStatusRepository,
            ReferenceDataServiceSupport support) {
        this.conceptStatusHarvester = conceptStatusHarvester;
        this.conceptStatusRepository = conceptStatusRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(conceptStatusRepository);
    }

    public Optional<ConceptStatus> getConceptStatus(String code) {
        return conceptStatusRepository.findByCode(code);
    }

    public ConceptStatuses getConceptStatuses() {
        return ConceptStatuses.builder().conceptStatuses(
                conceptStatusRepository.findAll().stream()
                        .sorted(Comparator.comparing(ConceptStatus::getUri))
                        .collect(Collectors.toList())).build();
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(conceptStatusHarvester, conceptStatusRepository, dbSourceID, "concept status");
    }
}
