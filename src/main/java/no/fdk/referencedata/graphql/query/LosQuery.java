package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.los.LosNode;
import no.fdk.referencedata.los.LosService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LosQuery {

    private final LosService losService;

    @QueryMapping
    public List<LosNode> losThemesAndWords(@Argument List<String> uris) {
        return uris != null ? losService.getByURIs(uris) : losService.getAll();
    }
}
