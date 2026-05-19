package no.fdk.referencedata.geonames;

public class LocalGeonamesHarvester extends GeonamesHarvester {

    private final String host;
    private final String port;

    public LocalGeonamesHarvester(String host, String port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String getApiUrl() {
        return "http://" + host + ":" + port;
    }

    @Override
    public String getUsername() {
        return "test";
    }
}
