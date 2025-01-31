package no.fdk.referencedata.eu.currency;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import no.fdk.referencedata.util.Version;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class CurrencyService {
    private final String dbSourceID = "currency-source";

    private final CurrencyHarvester currencyHarvester;
    private final CurrencyRepository currencyRepository;
    private final RDFSourceRepository rdfSourceRepository;
    private final HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    public CurrencyService(CurrencyHarvester currencyHarvester,
                           CurrencyRepository currencyRepository,
                           RDFSourceRepository rdfSourceRepository,
                           HarvestSettingsRepository harvestSettingsRepository) {
        this.currencyHarvester = currencyHarvester;
        this.currencyRepository = currencyRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return currencyRepository.count() == 0;
    }

    public Optional<Currency> getCurrency(String code) {
        return currencyRepository.findByCode(code);
    }

    public Currencies getCurrencies() {
        return Currencies.builder().currencies(
                StreamSupport.stream(currencyRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(Currency::getUri))
                        .collect(Collectors.toList())).build();
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
            final Version latestVersion = new Version(currencyHarvester.getVersion().replace("-", ""));

            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.CURRENCY.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.CURRENCY.name())
                            .latestVersion("0")
                            .build());

            final Version currentVersion = new Version(settings.getLatestVersion().replace("-", ""));

            if(force || latestVersion.compareTo(currentVersion) > 0) {
                currencyRepository.deleteAll();

                final AtomicInteger counter = new AtomicInteger(0);
                final Iterable<Currency> iterable = currencyHarvester.harvest().toIterable();
                iterable.forEach(item -> counter.getAndIncrement());
                log.info("Harvest and saving {} currencies", counter.get());
                currencyRepository.saveAll(iterable);

                RDFSource rdfSource = new RDFSource();
                rdfSource.setId(dbSourceID);
                rdfSource.setTurtle(RDFUtils.modelToResponse(currencyHarvester.getModel(), RDFFormat.TURTLE));
                rdfSourceRepository.save(rdfSource);

                settings.setLatestHarvestDate(LocalDateTime.now());
                settings.setLatestVersion(currencyHarvester.getVersion());
                harvestSettingsRepository.save(settings);
            }

        } catch(Exception e) {
            log.error("Unable to harvest currencies", e);
        }
    }
}
