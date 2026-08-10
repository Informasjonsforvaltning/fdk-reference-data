package no.fdk.referencedata;

/**
 * Metadata for a standard CodeList REST contract case.
 *
 * @param moduleId     registry module id
 * @param restPath     HTTP path (e.g. {@code /eu/frequencies})
 * @param jsonArrayField JSON wrapper property for the list response
 * @param expectedSize expected number of items after local harvest
 * @param sampleCode   known code for by-code 200; {@code null} when by-code is unsupported
 */
public record StandardCodeListContractCase(
        String moduleId,
        String restPath,
        String jsonArrayField,
        int expectedSize,
        String sampleCode
) {

    public boolean supportsByCode() {
        return sampleCode != null;
    }

    @Override
    public String toString() {
        return moduleId;
    }
}
