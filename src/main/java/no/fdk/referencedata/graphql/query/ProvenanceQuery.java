package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.provenancestatement.ProvenanceStatement;
import no.fdk.referencedata.provenancestatement.ProvenanceStatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProvenanceQuery {

    private final ProvenanceStatementService provenanceStatementService;

    @Autowired
    public ProvenanceQuery(ProvenanceStatementService provenanceStatementService) {
        this.provenanceStatementService = provenanceStatementService;
    }

    @QueryMapping
    public List<ProvenanceStatement> provenanceStatements() {
        return provenanceStatementService.getAll().stream()
                .sorted(Comparator.comparing(ProvenanceStatement::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public ProvenanceStatement provenanceStatementByCode(@Argument String code) {
        return provenanceStatementService.getByCode(code).orElse(null);
    }
}
