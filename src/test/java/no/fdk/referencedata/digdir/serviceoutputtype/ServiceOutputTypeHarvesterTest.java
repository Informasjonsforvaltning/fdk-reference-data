package no.fdk.referencedata.digdir.serviceoutputtype;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class ServiceOutputTypeHarvesterTest {

    @Test
    public void test_fetch_service_output_types() {
        ServiceOutputTypeHarvester harvester = LocalHarvesters.serviceOutputType();

        assertNotNull(harvester.getSource("service-output-type"));
        assertEquals("service-output-type.ttl", harvester.getSource("service-output-type").getFilename());

        List<ServiceOutputType> serviceOutputTypes = harvester.harvest().collectList().block();
        assertNotNull(serviceOutputTypes);
        assertEquals(8, serviceOutputTypes.size());

        ServiceOutputType permit = serviceOutputTypes.stream()
                .filter(t -> "permit".equals(t.getCode()))
                .findFirst()
                .orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/service-output-type#permit", permit.getUri());
        assertEquals("permit", permit.getLabel().get(Language.ENGLISH.code()));
        assertEquals("tillatelse", permit.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
    }

}
