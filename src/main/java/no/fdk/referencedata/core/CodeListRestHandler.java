package no.fdk.referencedata.core;

import org.apache.jena.riot.RDFFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Optional;
import java.util.Set;

@Component
public class CodeListRestHandler {

    static final Set<String> GENERIC_REST_EXCLUDED_MODULE_IDS = Set.of("los");

    public ServerResponse list(ReferenceDataModule module) {
        Object body = module.api().wrapAllSorted();
        return ServerResponse.ok().body(body);
    }

    public ServerResponse byCode(ReferenceDataModule module, ServerRequest request) {
        String pathVariable = module.api().byCodePathVariable();
        String code = request.pathVariable(pathVariable);
        Optional<?> item = module.api().findByCode(code);
        return item.<ServerResponse>map(value -> ServerResponse.ok().body(value))
                .orElseGet(() -> ServerResponse.notFound().build());
    }

    public ServerResponse rdf(ReferenceDataModule module) {
        if (!module.api().supportsRdf()) {
            return ServerResponse.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
        String turtle = module.api().getRdf(RDFFormat.TURTLE);
        return ServerResponse.ok()
                .contentType(MediaType.parseMediaType("text/turtle"))
                .body(turtle);
    }

    public ServerResponse harvest(ReferenceDataModule module) {
        if (!module.api().supportsHarvestPost() || !module.hasHarvestableService()) {
            return ServerResponse.notFound().build();
        }
        module.service().harvestAndSave();
        return ServerResponse.ok().build();
    }

    static boolean acceptsTurtle(ServerRequest request) {
        String accept = request.headers().firstHeader("Accept");
        return accept != null && accept.contains("text/turtle");
    }

    static boolean acceptsJsonList(ServerRequest request) {
        return !acceptsTurtle(request);
    }
}
