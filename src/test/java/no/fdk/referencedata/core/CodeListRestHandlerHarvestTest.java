package no.fdk.referencedata.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeListRestHandlerHarvestTest {

    @Mock
    private HarvestableReferenceData harvestable;

    private final CodeListRestHandler handler = new CodeListRestHandler();

    @Test
    void harvestReturnsOkOnSuccess() {
        when(harvestable.harvestAndSave()).thenReturn(HarvestResult.success(2));
        ReferenceDataModule module = moduleWithHarvest(true);

        ServerResponse response = handler.harvest(module);

        assertEquals(HttpStatus.OK, response.statusCode());
    }

    @Test
    void harvestReturnsInternalServerErrorOnFailure() {
        when(harvestable.harvestAndSave()).thenReturn(HarvestResult.failure());
        ReferenceDataModule module = moduleWithHarvest(true);

        ServerResponse response = handler.harvest(module);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
    }

    @Test
    void harvestReturnsInternalServerErrorOnSkippedEmpty() {
        when(harvestable.harvestAndSave()).thenReturn(HarvestResult.skippedEmpty());
        ReferenceDataModule module = moduleWithHarvest(true);

        ServerResponse response = handler.harvest(module);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
    }

    @Test
    void harvestReturnsNotFoundWhenHarvestPostUnsupported() {
        ReferenceDataModule module = moduleWithHarvest(false);

        ServerResponse response = handler.harvest(module);

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode());
    }

    private ReferenceDataModule moduleWithHarvest(boolean supportsHarvestPost) {
        CodeListApi<String> api = new CodeListApi<>(
                "/test",
                CodeListRepository.of(List::of, code -> Optional.empty()),
                null,
                list -> list,
                String.class,
                format -> "turtle",
                supportsHarvestPost,
                false,
                "code");
        return new ReferenceDataModule("test", harvestable, api);
    }
}
