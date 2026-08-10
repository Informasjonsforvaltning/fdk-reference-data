package no.fdk.referencedata.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class CodeListRestConfiguration {

    @Bean
    RouterFunction<ServerResponse> codeListRoutes(
            ReferenceDataRegistry registry,
            CodeListRestHandler handler) {
        RouterFunction<ServerResponse> routes = null;

        for (ReferenceDataModule module : registry.withApi()) {
            if (CodeListRestHandler.GENERIC_REST_EXCLUDED_MODULE_IDS.contains(module.id())) {
                continue;
            }
            routes = routes == null
                    ? routesFor(module, handler)
                    : routes.and(routesFor(module, handler));
        }

        return routes != null ? routes : RouterFunctions.route().build();
    }

    private static RouterFunction<ServerResponse> routesFor(
            ReferenceDataModule module,
            CodeListRestHandler handler) {
        CodeListApi<?> api = module.api();
        String path = api.restPath();

        RouterFunction<ServerResponse> routes = RouterFunctions.route(
                        RequestPredicates.GET(path).and(CodeListRestHandler::acceptsTurtle),
                        request -> handler.rdf(module))
                .andRoute(
                        RequestPredicates.GET(path).and(CodeListRestHandler::acceptsJsonList),
                        request -> handler.list(module));

        if (api.supportsHarvestPost()) {
            routes = routes.andRoute(RequestPredicates.POST(path), request -> handler.harvest(module));
        }

        if (api.supportsByCode()) {
            String lookupPath = path + "/{" + api.byCodePathVariable() + "}";
            routes = routes.andRoute(RequestPredicates.GET(lookupPath), request -> handler.byCode(module, request));
        }

        return routes;
    }
}
