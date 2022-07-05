package no.fdk.referencedata.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import no.fdk.referencedata.provenancestatement.ProvenanceStatement;
import no.fdk.referencedata.provenancestatement.ProvenanceStatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProvenanceQuery implements GraphQLQueryResolver {

    private final ProvenanceStatementService provenanceStatementService;

    @Autowired
    public ProvenanceQuery(ProvenanceStatementService provenanceStatementService) {
        this.provenanceStatementService = provenanceStatementService;
    }

    public List<ProvenanceStatement> getProvenanceStatements() {
        return provenanceStatementService.getAll().stream()
                .sorted(Comparator.comparing(ProvenanceStatement::getUri))
                .collect(Collectors.toList());
    }

    public ProvenanceStatement getProvenanceStatementByCode(String code) {
        return provenanceStatementService.getByCode(code).orElse(null);
    }
}
