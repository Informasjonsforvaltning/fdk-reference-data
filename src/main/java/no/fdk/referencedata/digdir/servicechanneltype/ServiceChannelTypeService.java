package no.fdk.referencedata.digdir.servicechanneltype;

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
public class ServiceChannelTypeService {
    private final String dbSourceID = "service-channel-types-source";

    private final ServiceChannelTypeHarvester serviceChannelTypeHarvester;

    private final ServiceChannelTypeWriter serviceChannelTypeWriter;

    private final ServiceChannelTypeRepository serviceChannelTypeRepository;

    private final HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ServiceChannelTypeService(
            ServiceChannelTypeHarvester serviceChannelTypeHarvester,
            ServiceChannelTypeRepository serviceChannelTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            HarvestSettingsRepository harvestSettingsRepository,
            ServiceChannelTypeWriter serviceChannelTypeWriter) {
        this.serviceChannelTypeHarvester = serviceChannelTypeHarvester;
        this.serviceChannelTypeRepository = serviceChannelTypeRepository;
        this.serviceChannelTypeWriter = serviceChannelTypeWriter;
        this.rdfSourceRepository = rdfSourceRepository;
        this.harvestSettingsRepository = harvestSettingsRepository;
    }

    public boolean firstTime() {
        return serviceChannelTypeRepository.count() == 0;
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
            final HarvestSettings settings = harvestSettingsRepository.findById(Settings.SERVICE_CHANNEL_TYPE.name())
                    .orElse(HarvestSettings.builder()
                            .id(Settings.SERVICE_CHANNEL_TYPE.name())
                            .latestVersion("0")
                            .build());

            final List<ServiceChannelType> items = new ArrayList<>();
            serviceChannelTypeHarvester.harvest().toIterable().forEach(items::add);
            log.info("Harvest and saving {} service-channel-types", items.size());

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(dbSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(serviceChannelTypeHarvester.getModel(), RDFFormat.TURTLE));

            settings.setLatestHarvestDate(LocalDateTime.now());
            settings.setLatestVersion(serviceChannelTypeHarvester.getVersion());

            serviceChannelTypeWriter.replaceAll(items, rdfSource, settings);
        } catch (Exception e) {
            log.error("Unable to harvest service-channel-types", e);
        }
    }
}
