package no.fdk.referencedata.core;

import org.apache.jena.riot.RDFFormat;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record CodeListApi<T>(
        String restPath,
        CodeListRepository<T> repository,
        Comparator<T> listSort,
        Function<List<T>, Object> wrapList,
        Class<T> itemType,
        Function<RDFFormat, String> rdfProvider,
        boolean supportsHarvestPost,
        boolean supportsByCode,
        String byCodePathVariable
) {

    public List<T> findAllSorted() {
        var stream = StreamSupport.stream(repository.findAll().spliterator(), false);
        if (listSort != null) {
            stream = stream.sorted(listSort);
        }
        return stream.collect(Collectors.toList());
    }

    public Object wrapAllSorted() {
        return wrapList.apply(findAllSorted());
    }

    public Optional<T> findByCode(String code) {
        return repository.findByCode(code);
    }

    public boolean supportsRdf() {
        return rdfProvider != null;
    }

    public String getRdf(RDFFormat format) {
        if (rdfProvider == null) {
            throw new UnsupportedOperationException("RDF is not supported for " + restPath);
        }
        return rdfProvider.apply(format);
    }
}
