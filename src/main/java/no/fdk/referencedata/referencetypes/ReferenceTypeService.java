package no.fdk.referencedata.referencetypes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReferenceTypeService {

    private final List<ReferenceType> types;

    @Autowired
    public ReferenceTypeService(ReferenceTypeImporter referenceTypeImporter) {
        this.types = List.copyOf(referenceTypeImporter.importFromSource());
    }

    public List<ReferenceType> getAll() {
        return types;
    }

    public Optional<ReferenceType> getByCode(final String code) {
        return types.stream()
                .filter(s -> s.code.equals(code))
                .findFirst();
    }

}
