package no.fdk.referencedata;

/**
 * Metadata for a standard CodeList GraphQL contract case.
 *
 * @param moduleId     registry module id
 * @param listField    GraphQL list field (e.g. {@code frequencies})
 * @param byCodeField  GraphQL by-code field; {@code null} when unsupported
 * @param expectedSize expected number of items after local harvest
 * @param sampleCode   known code for by-code; {@code null} when by-code is unsupported
 */
public record StandardCodeListGraphQlContractCase(
        String moduleId,
        String listField,
        String byCodeField,
        int expectedSize,
        String sampleCode
) {

    public boolean supportsByCode() {
        return byCodeField != null && sampleCode != null;
    }

    @Override
    public String toString() {
        return moduleId;
    }
}
