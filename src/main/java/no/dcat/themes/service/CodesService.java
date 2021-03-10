package no.dcat.themes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.dcat.datastore.domain.dcat.vocabulary.AT;
import no.dcat.datastore.domain.dcat.vocabulary.AdmEnhet;
import no.dcat.datastore.domain.dcat.vocabulary.GeoNames;
import no.dcat.shared.SkosCode;
import no.dcat.shared.Types;
import no.dcat.themes.database.TDBConnection;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Slf4j
@Scope("thread")
public class CodesService extends BaseServiceWithFraming {
    private static final String frame;

    static {
        try {
            frame = IOUtils.toString(BaseServiceWithFraming.class.getClassLoader().getResourceAsStream("frames/skosCode.json"), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    public CodesService(TDBConnection tdbConnection) {
        super(tdbConnection);
    }

    private static SkosCode extractLocation(Resource locationResource) {
        if (locationResource != null && locationResource.isURIResource()) {
            SkosCode location = new SkosCode();

            location.setUri(locationResource.getURI());
            location.setCode(locationResource.getURI());

            final Property[] locationNameProperties = {AdmEnhet.fylkesnavn, AdmEnhet.kommunenavn, AdmEnhet.nasjonnavn, GeoNames.officialName};

            Statement nameStatement = null;
            Property nameProperty = null;

            for (Property property : locationNameProperties) {
                nameStatement = locationResource.getProperty(property);
                if (nameStatement != null) {
                    nameProperty = property;
                    break;
                }
            }

            if (nameStatement != null && nameProperty != null) {

                StmtIterator nameStatementIterator = locationResource.listProperties(nameProperty);
                location.setPrefLabel(new HashMap<>());

                while (nameStatementIterator.hasNext()) {
                    Statement stmt = nameStatementIterator.next();

                    String language = stmt.getLanguage();
                    Literal literal = stmt.getObject().asLiteral();
                    String name = literal.getString();
                    if (language.isEmpty()) {
                        location.getPrefLabel().put("no", name);
                    } else {
                        if (!location.getPrefLabel().containsKey(language)) {
                            location.getPrefLabel().put(language, name);
                        }
                    }
                }
            }
            return location;
        }
        return null;
    }

    private static SkosCode extractSkosCode(Resource resource) {
        if (resource != null && resource.isURIResource()) {
            SkosCode skosCode = new SkosCode();
            skosCode.setUri(resource.getURI());

            if (resource.getProperty(AT.authorityCode) != null) {
                skosCode.setCode(resource.getProperty(AT.authorityCode).getString());
            } else {
                skosCode.setCode(resource.getURI());
            }

            if (resource.getProperty(DCTerms.isReplacedBy) != null) {
                skosCode.setIsReplacedBy(resource.getProperty(DCTerms.isReplacedBy).getString());
            }

            StmtIterator prefLabels = resource.listProperties(SKOS.prefLabel);
            skosCode.setPrefLabel(new HashMap<>());

            while (prefLabels.hasNext()) {
                Statement stmt = prefLabels.next();

                String language = stmt.getLanguage();
                Literal literal = stmt.getObject().asLiteral();
                String label = literal.getString();
                if (language.isEmpty()) {
                    skosCode.getPrefLabel().put("no", label);
                } else {
                    if (!skosCode.getPrefLabel().containsKey(language)) {
                        skosCode.getPrefLabel().put(language, label);
                    }
                }
            }
            return skosCode;
        }
        return null;
    }

    public List<String> listCodes() {
        return Arrays
                .stream(Types.values())
                .map(Types::getType)
                .collect(Collectors.toList());

    }

    @Cacheable("codes")
    public List<SkosCode> getCodes(Types type) {
        if (type.equals(Types.mediatypes)) {
            return readMediaTypeCodesFromFile();
        }

        return tdbConnection.inTransaction(ReadWrite.READ, connection -> {
            Dataset dataset = DatasetFactory.create(connection.getModelWithInference(type.toString()));
            List<SkosCode> result = new ArrayList<>();

            if (type.equals(Types.location)) {
                Model model = dataset.getDefaultModel();

                result.addAll(extractLocationCodes(model, model.listResourcesWithProperty(GeoNames.officialName)));
                result.addAll(extractLocationCodes(model, model.listResourcesWithProperty(RDF.type, AdmEnhet.NamedIndividual)));

            } else {
                try {
                    ResourceLoader resourceLoader = new DefaultResourceLoader();
                    Model model = ModelFactory.createDefaultModel();
                    model.read(type.getSourceUrl());
                    result.addAll(extractSkosCodes(model, model.listResourcesWithProperty(RDF.type, SKOS.Concept)));
                } catch (Exception e) {
                    System.out.println(e);
                }

            }

            return result;
        });

    }

    private List<SkosCode> extractLocationCodes(Model model, ResIterator resourceIterator) {
        if (model == null || resourceIterator == null) {
            return null;
        }


        List<SkosCode> result = new ArrayList<>();

        while (resourceIterator.hasNext()) {
            Resource resource = resourceIterator.next();
            result.add(extractLocation(resource));
        }

        return result;
    }

    public SkosCode addLocation(String locationUri) throws MalformedURLException {

        Model model = getRemoteModel(new URL(locationUri));

        tdbConnection.inTransaction(ReadWrite.WRITE, connection -> {
            connection.addModelToGraph(model, Types.location.toString());
            return null;
        });

        return getLocationCode(locationUri);

    }

    private SkosCode getLocationCode(String uri) {
        return tdbConnection.inTransaction(ReadWrite.READ, connection -> {
            Dataset dataset = DatasetFactory.create(connection.describeWithInference(uri));

            SkosCode locationCode = extractLocation(dataset.getDefaultModel().getResource(uri));

            dataset.close();

            return locationCode;

        });
    }

    private List<SkosCode> readMediaTypeCodesFromFile() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("json/mediatypes.json")) {
            return Stream.of(new ObjectMapper().readValue(inputStream, SkosCode[].class))
                .map(skosCode -> {
                    skosCode.setUri(String.format("%s/%s", "https://www.iana.org/assignments/media-types", skosCode.getCode()));

                    return skosCode;
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to read JSON file with media types", e);
        }
        return Collections.emptyList();
    }

    private List<SkosCode> extractSkosCodes(Model model, ResIterator resourceIterator) {
            if (model == null || resourceIterator == null) {
                return null;
            }

            List<SkosCode> result = new ArrayList<>();

            while (resourceIterator.hasNext()) {
                Resource resource = resourceIterator.next();
                result.add(extractSkosCode(resource));
            }

            return result;
        }
}
