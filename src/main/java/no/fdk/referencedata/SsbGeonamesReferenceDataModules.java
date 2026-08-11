package no.fdk.referencedata;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListApi;
import no.fdk.referencedata.core.CodeListApis;
import no.fdk.referencedata.core.CodeListRepository;
import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataModule;
import no.fdk.referencedata.geonames.GeonamesService;
import no.fdk.referencedata.geonorge.administrativeenheter.Enhet;
import no.fdk.referencedata.geonorge.administrativeenheter.EnhetRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.EnhetService;
import no.fdk.referencedata.geonorge.administrativeenheter.Enheter;
import no.fdk.referencedata.iana.mediatype.MediaTypeService;
import no.fdk.referencedata.los.LosNode;
import no.fdk.referencedata.los.LosNodes;
import no.fdk.referencedata.los.LosService;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjon;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonRepository;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonService;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjoner;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjon;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonRepository;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonService;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjoner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;

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
    private final FylkeOrganisasjonRepository fylkeOrganisasjonRepository;
    private final KommuneOrganisasjonService kommuneOrganisasjonService;
    private final KommuneOrganisasjonRepository kommuneOrganisasjonRepository;
    private final EnhetService enhetService;
    private final EnhetRepository enhetRepository;
    private final GeonamesService geonamesService;

    @Bean
    public ReferenceDataModule mediaTypeModule() {
        return new ReferenceDataModule("media-type", mediaTypeService, CRON_MEDIA_TYPE);
    }

    @Bean
    public ReferenceDataModule losModule() {
        return module("los", losService, losApi(), CRON_LOS);
    }

    @Bean
    public CodeListApi<LosNode> losApi() {
        return CodeListApis.listWithRdf(
                "/los/themes-and-words",
                CodeListRepository.listOnly(() -> losService.getAll()),
                CodeListApis.sortByUri(LosNode::getUri),
                list -> LosNodes.builder().losNodes(list).build(),
                losService::getRdf,
                LosNode.class);
    }

    @Bean
    public ReferenceDataModule fylkeOrganisasjonModule() {
        return module("fylke-organisasjon", fylkeOrganisasjonService, fylkeOrganisasjonApi(), CRON_FYLKE_ORGANISASJON);
    }

    @Bean
    public CodeListApi<FylkeOrganisasjon> fylkeOrganisasjonApi() {
        return CodeListApis.withLookup(
                "/ssb/fylke-organisasjoner",
                CodeListRepository.of(
                        fylkeOrganisasjonRepository::findAll,
                        fylkeOrganisasjonRepository::findByFylkesnummer),
                Comparator.comparing(FylkeOrganisasjon::getOrganisasjonsnummer),
                list -> FylkeOrganisasjoner.builder().fylkeOrganisasjoner(list).build(),
                null,
                "fylkesnummer",
                FylkeOrganisasjon.class);
    }

    @Bean
    public ReferenceDataModule kommuneOrganisasjonModule() {
        return module("kommune-organisasjon", kommuneOrganisasjonService, kommuneOrganisasjonApi(), CRON_KOMMUNE_ORGANISASJON);
    }

    @Bean
    public CodeListApi<KommuneOrganisasjon> kommuneOrganisasjonApi() {
        return CodeListApis.withLookup(
                "/ssb/kommune-organisasjoner",
                CodeListRepository.of(
                        kommuneOrganisasjonRepository::findAll,
                        kommuneOrganisasjonRepository::findByKommunenummer),
                Comparator.comparing(KommuneOrganisasjon::getOrganisasjonsnummer),
                list -> KommuneOrganisasjoner.builder().kommuneOrganisasjoner(list).build(),
                null,
                "kommunenummer",
                KommuneOrganisasjon.class);
    }

    @Bean
    public ReferenceDataModule enhetModule() {
        return module("administrative-enhet", enhetService, enhetApi(), CRON_ENHET);
    }

    @Bean
    public CodeListApi<Enhet> enhetApi() {
        return CodeListApis.standard(
                "/geonorge/administrative-enheter",
                CodeListRepository.of(enhetRepository::findAll, enhetRepository::findByCode),
                CodeListApis.sortByUri(Enhet::getUri),
                list -> Enheter.builder().enheter(list).build(),
                enhetService::getRdf,
                Enhet.class);
    }

    @Bean
    public ReferenceDataModule geonamesModule() {
        return new ReferenceDataModule("geonames", geonamesService, CRON_GEONAMES);
    }

    private static ReferenceDataModule module(String id, HarvestableReferenceData service, CodeListApi<?> api, String cron) {
        return new ReferenceDataModule(id, service, api, cron);
    }
}
