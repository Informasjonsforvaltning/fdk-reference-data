package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.los.LosNode;
import no.fdk.referencedata.los.LosService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LosQuery implements GraphQLQueryResolver {

    private final LosService losService;

    public List<LosNode> getLosThemesAndWords(List<String> uris) {
        return uris != null ? losService.getByURIs(uris) : losService.getAll();
    }
}