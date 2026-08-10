package no.fdk.referencedata;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.ReferenceDataModule;
import no.fdk.referencedata.geonames.GeonamesService;
import no.fdk.referencedata.geonorge.administrativeenheter.EnhetService;
import no.fdk.referencedata.iana.mediatype.MediaTypeService;
import no.fdk.referencedata.los.LosService;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonService;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class SsbGeonamesReferenceDataModules {

    static final String CRON_MEDIA_TYPE = "0 40 1 1 * ?";
    static final String CRON_LOS = "0 50 2 1 * ?";
    static final String CRON_FYLKE_ORGANISASJON = "0 10 3 1 * ?";
    static final String CRON_KOMMUNE_ORGANISASJON = "0 20 3 1 * ?";
    static final String CRON_ENHET = "0 0 4 1 * ?";
    static final String CRON_GEONAMES = "0 30 5 1 * ?";

    private final MediaTypeService mediaTypeService;
    private final LosService losService;
    private final FylkeOrganisasjonService fylkeOrganisasjonService;
    private final KommuneOrganisasjonService kommuneOrganisasjonService;
    private final EnhetService enhetService;
    private final GeonamesService geonamesService;

    @Bean
    public ReferenceDataModule mediaTypeModule() {
        return new ReferenceDataModule("media-type", mediaTypeService);
    }

    @Scheduled(cron = CRON_MEDIA_TYPE)
    public void updateMediaTypes() {
        mediaTypeService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule losModule() {
        return new ReferenceDataModule("los", losService);
    }

    @Scheduled(cron = CRON_LOS)
    public void updateLos() {
        losService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule fylkeOrganisasjonModule() {
        return new ReferenceDataModule("fylke-organisasjon", fylkeOrganisasjonService);
    }

    @Scheduled(cron = CRON_FYLKE_ORGANISASJON)
    public void updateFylkeskommuner() {
        fylkeOrganisasjonService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule kommuneOrganisasjonModule() {
        return new ReferenceDataModule("kommune-organisasjon", kommuneOrganisasjonService);
    }

    @Scheduled(cron = CRON_KOMMUNE_ORGANISASJON)
    public void updateKommuneOrganisasjoner() {
        kommuneOrganisasjonService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule enhetModule() {
        return new ReferenceDataModule("administrative-enhet", enhetService);
    }

    @Scheduled(cron = CRON_ENHET)
    public void updateAdministrativeEnheter() {
        enhetService.harvestAndSave();
    }

    @Bean
    public ReferenceDataModule geonamesModule() {
        return new ReferenceDataModule("geonames", geonamesService);
    }

    @Scheduled(cron = CRON_GEONAMES)
    public void updateGeonames() {
        geonamesService.harvestAndSave();
    }
}
