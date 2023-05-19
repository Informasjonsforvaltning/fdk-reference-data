package no.fdk.referencedata.ssb.kommuneorganisasjoner;

public class LocalKommuneOrganisasjonHarvester extends KommuneOrganisasjonHarvester {

    private final String host;
    private final String port;

    public LocalKommuneOrganisasjonHarvester(String host, String port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String getApiUrl() {
        return "http://" + host + ":" + port;
    }
}
