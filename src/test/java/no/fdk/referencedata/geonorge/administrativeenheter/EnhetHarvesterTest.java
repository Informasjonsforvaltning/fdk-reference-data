package no.fdk.referencedata.geonorge.administrativeenheter;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;

import static no.fdk.referencedata.geonorge.administrativeenheter.LocalEnhetHarvester.ADMINISTRATIVE_ENHETER_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
                "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class EnhetHarvesterTest extends AbstractContainerTest {

    @Test
    public void test_fetch_administrative_enheter() {
        LocalEnhetHarvester harvester = new LocalEnhetHarvester();

        List<Enhet> enheter = harvester.harvest().collectList().block();
        assertNotNull(enheter);
        assertEquals(ADMINISTRATIVE_ENHETER_SIZE, enheter.size());

        Enhet first = enheter.stream().sorted(Comparator.comparing(Enhet::getUri)).findFirst().get();
        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/173143173142", first.getUri());
        assertEquals("Troms og Finnmark", first.getName());
        assertEquals("173143173142", first.getCode());
    }

}
