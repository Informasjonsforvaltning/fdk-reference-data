package no.fdk.referencedata.ssb.fylkeorganisasjoner;

public class LocalFylkeOrganisasjonHarvester extends FylkeOrganisasjonHarvester {

    private final String host;
    private final String port;

    public LocalFylkeOrganisasjonHarvester(String host, String port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String getApiUrl() {
        return "http://" + host + ":" + port;
    }
}
