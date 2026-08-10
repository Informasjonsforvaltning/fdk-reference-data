package no.fdk.referencedata.core;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeListApiTest {

    record Item(String uri, String code) {}

    @Test
    void findAllSortedAndWrap() {
        CodeListApi<Item> api = CodeListApis.standard(
                "/test/items",
                CodeListRepository.of(
                        () -> List.of(new Item("c", "C"), new Item("a", "A"), new Item("b", "B")),
                        code -> Optional.of(new Item(code.toLowerCase(), code))),
                Comparator.comparing(Item::uri),
                list -> list,
                format -> "turtle",
                Item.class);

        assertEquals(List.of("A", "B", "C"), api.findAllSorted().stream().map(Item::code).toList());
        assertEquals("A", api.findByCode("A").map(Item::code).orElseThrow());
        assertTrue(api.supportsRdf());
        assertTrue(api.supportsHarvestPost());
        assertTrue(api.supportsByCode());
        assertEquals("code", api.byCodePathVariable());
    }

    @Test
    void listWithRdfDisablesByCode() {
        CodeListApi<Item> api = CodeListApis.listWithRdf(
                "/test/items",
                CodeListRepository.listOnly(() -> List.of(new Item("a", "A"))),
                Comparator.comparing(Item::uri),
                list -> list,
                format -> "turtle",
                Item.class);

        assertFalse(api.supportsByCode());
        assertTrue(api.findByCode("A").isEmpty());
    }

    @Test
    void readOnlyPreservesOrderAndDisablesHarvest() {
        CodeListApi<Item> api = CodeListApis.readOnly(
                "/test/items",
                CodeListRepository.of(
                        () -> List.of(new Item("c", "C"), new Item("a", "A")),
                        code -> Optional.empty()),
                null,
                list -> list,
                null,
                Item.class);

        assertEquals(List.of("C", "A"), api.findAllSorted().stream().map(Item::code).toList());
        assertFalse(api.supportsHarvestPost());
        assertFalse(api.supportsRdf());
        assertTrue(api.supportsByCode());
    }
}
