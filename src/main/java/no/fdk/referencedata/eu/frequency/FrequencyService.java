package no.fdk.referencedata.eu.frequency;

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
public class FrequencyService {
    private final String dbSourceID = "frequencies-source";

    private final FrequencyHarvester frequencyHarvester;

    private final FrequencyWriter frequencyWriter;

    private final FrequencyRepository frequencyRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public FrequencyService(
            FrequencyHarvester frequencyHarvester,
            FrequencyRepository frequencyRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            FrequencyWriter frequencyWriter) {
        this.frequencyHarvester = frequencyHarvester;
        this.frequencyRepository = frequencyRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
        this.frequencyWriter = frequencyWriter;
    }

    public boolean firstTime() {
        return frequencyRepository.count() == 0;
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
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.FREQUENCY.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.FREQUENCY.name())
                            .latestVersion("0")
                            .build());

            final List<Frequency> items = new ArrayList<>();
            frequencyHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} frequencies", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(frequencyHarvester.getModel(), RDFFormat.TURTLE));

            settings.setLatestHarvestDate(LocalDateTime.now());
            settings.setLatestVersion(frequencyHarvester.getVersion());

            frequencyWriter.replaceAll(items, rdfSource, settings);
        } catch (Exception e) {
            log.error("Unable to harvest frequencies", e);
        }
    }
}
