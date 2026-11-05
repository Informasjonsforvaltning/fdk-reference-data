package no.fdk.referencedata.lovdata;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import java.util.List;
import java.util.Map;

public final class LegalResourceModel {

    private static final String ELI_NS = "http://data.europa.eu/eli/ontology#";
    private static final String TYPE_VOCABULARY = "https://data.norge.no/vocabulary/legal-resource-type#";

    private static final Map<String, String> TYPE_URIS = Map.of(
            "lov", TYPE_VOCABULARY + "act",
            "forskrift", TYPE_VOCABULARY + "regulation");

    private LegalResourceModel() {
    }

    public static Model from(List<LegalResource> legalResources) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("dct", DCTerms.NS);
        model.setNsPrefix("eli", ELI_NS);
        legalResources.forEach(legalResource -> add(model, legalResource));
        return model;
    }

    private static void add(Model model, LegalResource legalResource) {
        Resource resource = model.createResource(legalResource.getUri())
                .addProperty(RDF.type, model.createResource(ELI_NS + "LegalResource"));

        if (legalResource.getIdentifier() != null) {
            resource.addProperty(DCTerms.identifier, legalResource.getIdentifier());
        }

        String typeUri = TYPE_URIS.get(legalResource.getType());
        if (typeUri != null) {
            resource.addProperty(DCTerms.type, model.createResource(typeUri));
        }

        if (legalResource.getLabel() != null) {
            legalResource.getLabel().forEach((language, title) ->
                    resource.addProperty(DCTerms.title, title, language));
        }
    }
}
