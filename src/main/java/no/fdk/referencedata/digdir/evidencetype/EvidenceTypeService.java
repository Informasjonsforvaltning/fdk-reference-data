package no.fdk.referencedata.digdir.evidencetype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EvidenceTypeService {
    private final String dbSourceID = "evidence-types-source";

    private final EvidenceTypeHarvester evidenceTypeHarvester;

    private final EvidenceTypeWriter evidenceTypeWriter;

    private final EvidenceTypeRepository evidenceTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public EvidenceTypeService(
            EvidenceTypeHarvester evidenceTypeHarvester,
            EvidenceTypeRepository evidenceTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            EvidenceTypeWriter evidenceTypeWriter) {
        this.evidenceTypeHarvester = evidenceTypeHarvester;
        this.evidenceTypeRepository = evidenceTypeRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.evidenceTypeWriter = evidenceTypeWriter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public boolean firstTime() {
        return evidenceTypeRepository.count() == 0;
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
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.EVIDENCE_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.EVIDENCE_TYPE.name())
                            .build());

            final List<EvidenceType> items = new ArrayList<>();
            evidenceTypeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} evidence-types", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(evidenceTypeHarvester.getModel(), RDFFormat.TURTLE));

            settings.setLatestHarvestDate(LocalDateTime.now());

            evidenceTypeWriter.replaceAll(items, rdfSource, settings);
        } catch (Exception e) {
            log.error("Unable to harvest evidence-types", e);
        }
    }
}
