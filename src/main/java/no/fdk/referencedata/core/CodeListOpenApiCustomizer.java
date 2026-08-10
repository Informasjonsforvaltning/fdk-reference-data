package no.fdk.referencedata.core;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeListOpenApiCustomizer implements OpenApiCustomizer {

    private final ReferenceDataRegistry registry;

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }
        if (openApi.getPaths() == null) {
            openApi.setPaths(new io.swagger.v3.oas.models.Paths());
        }

        for (ReferenceDataModule module : registry.withApi()) {
            if (CodeListRestHandler.GENERIC_REST_EXCLUDED_MODULE_IDS.contains(module.id())) {
                continue;
            }
            documentModule(openApi, module);
        }
    }

    private void documentModule(OpenAPI openApi, ReferenceDataModule module) {
        CodeListApi<?> api = module.api();
        String path = api.restPath();
        String tag = tagFor(path);

        PathItem pathItem = openApi.getPaths().computeIfAbsent(path, ignored -> new PathItem());
        pathItem.setGet(listOperation(openApi, module, api, tag));

        if (api.supportsHarvestPost()) {
            pathItem.setPost(harvestOperation(module, tag));
        }

        if (api.supportsByCode()) {
            String lookupPath = path + "/{" + api.byCodePathVariable() + "}";
            PathItem lookupItem = openApi.getPaths().computeIfAbsent(lookupPath, ignored -> new PathItem());
            lookupItem.setGet(byCodeOperation(openApi, module, api, tag));
        }
    }

    private Operation listOperation(
            OpenAPI openApi,
            ReferenceDataModule module,
            CodeListApi<?> api,
            String tag) {
        Content content = new Content()
                .addMediaType(
                        org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(schemaRef(openApi, api.listWrapperType())));

        if (api.supportsRdf()) {
            content.addMediaType(
                    "text/turtle",
                    new MediaType().schema(new StringSchema()));
        }

        return new Operation()
                .operationId(module.id() + ".list")
                .summary("List " + module.id())
                .addTagsItem(tag)
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                .description("OK")
                                .content(content)));
    }

    private Operation byCodeOperation(
            OpenAPI openApi,
            ReferenceDataModule module,
            CodeListApi<?> api,
            String tag) {
        String pathVariable = api.byCodePathVariable();
        return new Operation()
                .operationId(module.id() + ".byCode")
                .summary("Get " + module.id() + " by " + pathVariable)
                .addTagsItem(tag)
                .addParametersItem(new Parameter()
                        .name(pathVariable)
                        .in("path")
                        .required(true)
                        .schema(new StringSchema()))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                .description("OK")
                                .content(new Content().addMediaType(
                                        org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                        new MediaType().schema(schemaRef(openApi, api.itemType())))))
                        .addApiResponse("404", new ApiResponse().description("Not found")));
    }

    private Operation harvestOperation(ReferenceDataModule module, String tag) {
        return new Operation()
                .operationId(module.id() + ".harvest")
                .summary("Harvest " + module.id())
                .addTagsItem(tag)
                .addSecurityItem(new SecurityRequirement().addList("apiKey"))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse().description("OK")));
    }

    private Schema<?> schemaRef(OpenAPI openApi, Class<?> type) {
        ResolvedSchema resolved = ModelConverters.getInstance()
                .readAllAsResolvedSchema(new AnnotatedType().type(type));
        if (resolved.referencedSchemas != null) {
            resolved.referencedSchemas.forEach(openApi.getComponents()::addSchemas);
        }
        if (resolved.schema != null && resolved.schema.getName() != null) {
            openApi.getComponents().addSchemas(resolved.schema.getName(), resolved.schema);
            return new Schema<>().$ref("#/components/schemas/" + resolved.schema.getName());
        }
        return resolved.schema != null ? resolved.schema : new Schema<>().type("object");
    }

    private static String tagFor(String path) {
        String[] parts = path.split("/");
        return parts.length > 1 ? parts[1] : path;
    }
}
