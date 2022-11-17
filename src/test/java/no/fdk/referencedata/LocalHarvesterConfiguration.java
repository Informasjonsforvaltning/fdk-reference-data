package no.fdk.referencedata;

import no.fdk.referencedata.digdir.roletype.LocalRoleTypeHarvester;
import no.fdk.referencedata.digdir.roletype.RoleTypeHarvester;
import no.fdk.referencedata.eu.accessright.AccessRightHarvester;
import no.fdk.referencedata.eu.accessright.LocalAccessRightHarvester;
import no.fdk.referencedata.eu.datatheme.DataThemeHarvester;
import no.fdk.referencedata.eu.datatheme.LocalDataThemeHarvester;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeHarvester;
import no.fdk.referencedata.eu.distributiontype.LocalDistributionTypeHarvester;
import no.fdk.referencedata.eu.eurovoc.EuroVocHarvester;
import no.fdk.referencedata.eu.eurovoc.LocalEuroVocHarvester;
import no.fdk.referencedata.eu.filetype.FileTypeHarvester;
import no.fdk.referencedata.eu.filetype.LocalFileTypeHarvester;
import no.fdk.referencedata.eu.frequency.FrequencyHarvester;
import no.fdk.referencedata.eu.frequency.LocalFrequencyHarvester;
import no.fdk.referencedata.eu.mainactivity.LocalMainActivityHarvester;
import no.fdk.referencedata.eu.mainactivity.MainActivityHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.LocalFylkeHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneHarvester;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.LocalKommuneHarvester;
import no.fdk.referencedata.iana.mediatype.LocalMediaTypeHarvester;
import no.fdk.referencedata.iana.mediatype.MediaTypeHarvester;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class LocalHarvesterConfiguration {

    @Value("${wiremock.host:dummy}")
    private String wiremockHost;

    @Value("${wiremock.port:0}")
    private String wiremockPort;

    @Bean
    public MediaTypeHarvester mediaTypeHarvester() {
        return new LocalMediaTypeHarvester();
    }

    @Bean
    public FileTypeHarvester fileTypeHarvester() {
        return new LocalFileTypeHarvester("1");
    }

    @Bean
    public DataThemeHarvester dataThemeHarvester() {
        return new LocalDataThemeHarvester("1");
    }

    @Bean
    public EuroVocHarvester euroVocHarvester() {
        return new LocalEuroVocHarvester("1");
    }

    @Bean
    public AccessRightHarvester accessRightHarvester() {
        return new LocalAccessRightHarvester("1");
    }

    @Bean
    public KommuneHarvester kommuneHarvester() {
        return new LocalKommuneHarvester(wiremockHost, wiremockPort);
    }

    @Bean
    public FylkeHarvester fylkeHarvester() {
        return new LocalFylkeHarvester(wiremockHost, wiremockPort);
    }

    @Bean
    public FrequencyHarvester frequencyHarvester() {
        return new LocalFrequencyHarvester("1");
    }

    @Bean
    public DistributionTypeHarvester distributionTypeHarvester() {
        return new LocalDistributionTypeHarvester("1");
    }

    @Bean
    public MainActivityHarvester mainActivityHarvester() {
        return new LocalMainActivityHarvester("1");
    }

    @Bean
    public RoleTypeHarvester roleTypeHarvester() {
        return new LocalRoleTypeHarvester("1");
    }
}
