package no.fdk.referencedata.core;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CodeListRepository<T> {

    Iterable<T> findAll();

    Optional<T> findByCode(String code);

    static <T> CodeListRepository<T> of(
            Supplier<Iterable<T>> findAll,
            Function<String, Optional<T>> findByCode) {
        return new CodeListRepository<>() {
            @Override
            public Iterable<T> findAll() {
                return findAll.get();
            }

            @Override
            public Optional<T> findByCode(String code) {
                return findByCode.apply(code);
            }
        };
    }

    static <T> CodeListRepository<T> listOnly(Supplier<Iterable<T>> findAll) {
        return of(findAll, code -> Optional.empty());
    }
}
