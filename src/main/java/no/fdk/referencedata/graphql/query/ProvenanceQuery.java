package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.provenancestatement.ProvenanceStatement;
import no.fdk.referencedata.provenancestatement.ProvenanceStatementService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProvenanceQuery {

    private final ProvenanceStatementService provenanceStatementService;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<ProvenanceStatement> provenanceStatements() {
        return support.allSortedByUri(provenanceStatementService.getAll(), ProvenanceStatement::getUri);
    }

    @QueryMapping
    public ProvenanceStatement provenanceStatementByCode(@Argument String code) {
        return support.byCode(provenanceStatementService::getByCode, code);
    }
}
