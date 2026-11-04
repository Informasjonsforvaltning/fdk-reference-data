package no.fdk.referencedata.lovdata;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegalResourceModelTest {

    private static final String URI = "https://lovdata.no/eli/forskrift/1896/06/26/9805";
    private static final String TITLE =
            "Forskrifter angaaende Fremgangsmaaden ved Salg af Leilændingsgods tilhørende Stamhuse m.V.";

    private LegalResource regulation() {
        return LegalResource.builder()
                .uri(URI)
                .identifier("FOR-1896-06-26-9805")
                .label(Map.of("nb", TITLE))
                .type("forskrift")
                .build();
    }

    @Test
    void serialisesRegulationAsAgreed() {
        Model model = LegalResourceModel.from(List.of(regulation()));
        var subject = ResourceFactory.createResource(URI);

        assertTrue(model.contains(subject, RDF.type,
                ResourceFactory.createResource("http://data.europa.eu/eli/ontology#LegalResource")));
        assertTrue(model.contains(subject, DCTerms.identifier, "FOR-1896-06-26-9805"));
        assertTrue(model.contains(subject, DCTerms.title,
                ResourceFactory.createLangLiteral(TITLE, "nb")));
        assertTrue(model.contains(subject, DCTerms.type, ResourceFactory.createResource(
                "https://data.norge.no/vocabulary/legal-resource-type#regulation")));
    }

    @Test
    void mapsActToTheActConcept() {
        Model model = LegalResourceModel.from(List.of(LegalResource.builder()
                .uri("https://lovdata.no/eli/lov/1998/08/21/65")
                .label(Map.of("nb", "Lov"))
                .type("lov")
                .build()));

        assertTrue(model.contains(
                ResourceFactory.createResource("https://lovdata.no/eli/lov/1998/08/21/65"),
                DCTerms.type,
                ResourceFactory.createResource("https://data.norge.no/vocabulary/legal-resource-type#act")));
    }

    @Test
    void writesOneTitlePerLanguage() {
        Model model = LegalResourceModel.from(List.of(LegalResource.builder()
                .uri("https://lovdata.no/eli/lov/1814/05/17/0")
                .label(Map.of("nb", "Kongeriket Norges Grunnlov", "nn", "Kongeriket Noregs grunnlov"))
                .type("lov")
                .build()));
        var subject = ResourceFactory.createResource("https://lovdata.no/eli/lov/1814/05/17/0");

        assertTrue(model.contains(subject, DCTerms.title,
                ResourceFactory.createLangLiteral("Kongeriket Norges Grunnlov", "nb")));
        assertTrue(model.contains(subject, DCTerms.title,
                ResourceFactory.createLangLiteral("Kongeriket Noregs grunnlov", "nn")));
    }

    @Test
    void omitsIdentifierWhenMissing() {
        Model model = LegalResourceModel.from(List.of(LegalResource.builder()
                .uri(URI)
                .label(Map.of("nb", TITLE))
                .type("forskrift")
                .build()));

        assertFalse(model.contains(ResourceFactory.createResource(URI), DCTerms.identifier));
    }
}
