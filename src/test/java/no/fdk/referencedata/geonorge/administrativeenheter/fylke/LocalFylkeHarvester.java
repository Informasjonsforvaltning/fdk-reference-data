package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

public class LocalFylkeHarvester extends FylkeHarvester {

    private final String host;
    private final String port;

    public LocalFylkeHarvester(String host, String port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String getApiUrl() {
        return "http://" + host + ":" + port;
    }
}
