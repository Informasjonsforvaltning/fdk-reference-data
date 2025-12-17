package no.fdk.referencedata.eu.eurovoc;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.util.Version;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class EuroVocService {
    private final String dbSourceID = "eurovoc-source";

    private final EuroVocHarvester euroVocHarvester;

    private final EuroVocRepository euroVocRepository;

    private final RDFSourceRepository rdfSourceRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public EuroVocService(EuroVocHarvester euroVocHarvester,
                          EuroVocRepository euroVocRepository,
                          RDFSourceRepository rdfSourceRepository,
                          HarvestSettingsRepository harvestSettingsRepository) {
        this.euroVocHarvester = euroVocHarvester;
        this.euroVocRepository = euroVocRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return true;
    }

    public String getRdf(RDFFormat rdfFormat) {
        String source = rdfSourceRepository.findById(dbSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    @Transactional
    public void harvestAndSave(boolean force) {
        try {
            final Version latestVersion = new Version(euroVocHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.EURO_VOC.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.EURO_VOC.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                euroVocRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<EuroVoc> iterable = euroVocHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} eurovocs", counter.get());
                euroVocRepository.saveAll(iterable);

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(euroVocHarvester.getModel(), RDFFormat.TURTLE));
                rdfSourceRepository.save(rdfSource);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(euroVocHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest eurovoc", e);
        }
    }
}
