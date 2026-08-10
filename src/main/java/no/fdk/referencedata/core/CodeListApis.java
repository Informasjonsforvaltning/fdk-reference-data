package no.fdk.referencedata.core;

import org.apache.jena.riot.RDFFormat;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class CodeListApis {

    private CodeListApis() {}

    public static <T> CodeListApi<T> standard(
            String restPath,
            CodeListRepository<T> repository,
            Comparator<T> listSort,
            Function<List<T>, Object> wrapList,
            Function<RDFFormat, String> rdfProvider,
            Class<T> itemType) {
        return new CodeListApi<>(
                restPath,
                repository,
                listSort,
                wrapList,
                itemType,
                rdfProvider,
                true,
                true,
                "code");
    }

    public static <T> CodeListApi<T> listWithRdf(
            String restPath,
            CodeListRepository<T> repository,
            Comparator<T> listSort,
            Function<List<T>, Object> wrapList,
            Function<RDFFormat, String> rdfProvider,
            Class<T> itemType) {
        return new CodeListApi<>(
                restPath,
                repository,
                listSort,
                wrapList,
                itemType,
                rdfProvider,
                true,
                false,
                "code");
    }

    public static <T> CodeListApi<T> withLookup(
            String restPath,
            CodeListRepository<T> repository,
            Comparator<T> listSort,
            Function<List<T>, Object> wrapList,
            Function<RDFFormat, String> rdfProvider,
            String byCodePathVariable,
            Class<T> itemType) {
        return new CodeListApi<>(
                restPath,
                repository,
                listSort,
                wrapList,
                itemType,
                rdfProvider,
                true,
                true,
                byCodePathVariable);
    }

    public static <T> CodeListApi<T> readOnly(
            String restPath,
            CodeListRepository<T> repository,
            Comparator<T> listSort,
            Function<List<T>, Object> wrapList,
            Function<RDFFormat, String> rdfProvider,
            Class<T> itemType) {
        return new CodeListApi<>(
                restPath,
                repository,
                listSort,
                wrapList,
                itemType,
                rdfProvider,
                false,
                true,
                "code");
    }

    public static <T> Comparator<T> sortByUri(Function<T, String> uriExtractor) {
        return Comparator.comparing(uriExtractor);
    }
}
