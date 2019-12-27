package no.dcat.themes.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class GDPRService {
    private final ObjectReader objectReader = new ObjectMapper().readerFor(new TypeReference<List<String>>() {});

    public List<String> getAllProcessingBasis() {

        try {
            Path f = Paths.get(ClassLoader.getSystemResource("gdpr/processing-basis.json").toURI());
            return objectReader.readValue(new ByteArrayInputStream(Files.readAllBytes(f)));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
