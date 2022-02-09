package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

public class LocalKommuneHarvester extends KommuneHarvester {

    private final String host;
    private final String port;

    public LocalKommuneHarvester(String host, String port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String getApiUrl() {
        return "http://" + host + ":" + port;
    }
}
