package no.fdk.referencedata.digdir.servicechanneltype;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class ServiceChannelTypeHarvesterTest {

    @Test
    public void test_fetch_service_channel_types() {
        ServiceChannelTypeHarvester harvester = new LocalServiceChannelTypeHarvester("123-0");

        assertNotNull(harvester.getSource("service-channel-type"));
        assertEquals("service-channel-type.ttl", harvester.getSource("service-channel-type").getFilename());
        assertEquals("123-0", harvester.getVersion());

        List<ServiceChannelType> serviceChannelTypes = harvester.harvest().collectList().block();
        assertNotNull(serviceChannelTypes);
        assertEquals(11, serviceChannelTypes.size());

        ServiceChannelType first = serviceChannelTypes.get(0);
        assertEquals("https://data.norge.no/vocabulary/service-channel-type#client-location", first.getUri());
        assertEquals("client-location", first.getCode());
        assertEquals("cient’s location", first.getLabel().get(Language.ENGLISH.code()));
    }

}
