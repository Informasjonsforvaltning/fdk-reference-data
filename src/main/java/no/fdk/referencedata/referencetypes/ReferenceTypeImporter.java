package no.fdk.referencedata.referencetypes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ReferenceTypeImporter {

    List<ReferenceType> importFromSource() {
        try (InputStream in = ReferenceTypeImporter.class.getClassLoader().getResourceAsStream("json/reference-types.json")) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, new TypeReference<>(){});
        }
        catch(Exception e){
            log.error("Unable to import reference types", e);
            return Collections.emptyList();
        }
    }
}
