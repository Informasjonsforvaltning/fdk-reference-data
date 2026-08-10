package no.fdk.referencedata.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Component
public class CodeListQuerySupport {

    public <T> List<T> allSortedByUri(JpaRepository<T, String> repository, Function<T, String> uriExtractor) {
        return allSorted(repository.findAll(), Comparator.comparing(uriExtractor));
    }

    public <T> List<T> allSortedByUri(Iterable<T> items, Function<T, String> uriExtractor) {
        return allSorted(items, Comparator.comparing(uriExtractor));
    }

    public <T> List<T> allSorted(Iterable<T> items, Comparator<T> comparator) {
        return StreamSupport.stream(items.spliterator(), false)
                .sorted(comparator)
                .toList();
    }

    public <T> T byCode(Function<String, Optional<T>> findByCode, String code) {
        return findByCode.apply(code).orElse(null);
    }
}
