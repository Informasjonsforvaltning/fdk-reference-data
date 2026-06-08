package no.fdk.referencedata.eu.language;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalLanguageHarvester extends LanguageHarvester {
    public static final int LANGUAGES_SIZE = 3;

    @Override
    public Resource getSource() {
        return new ClassPathResource("language-sparql-result.ttl");
    }
}
