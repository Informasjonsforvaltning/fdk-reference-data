package no.fdk.referencedata.referencetypes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReferenceTypeService {

    public ReferenceTypeImporter referenceTypeImporter;

    @Autowired
    public ReferenceTypeService(ReferenceTypeImporter referenceTypeImporter) {
        this.referenceTypeImporter = referenceTypeImporter;
    }

    public List<ReferenceType> getAll() {
        return referenceTypeImporter.importFromSource();
    }

    public Optional<ReferenceType> getByCode(final String code) {
        return referenceTypeImporter.importFromSource().stream()
                .filter(s -> s.code.equals(code))
                .findFirst();
    }

}
