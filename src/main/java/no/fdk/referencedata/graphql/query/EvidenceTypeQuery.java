package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.digdir.evidencetype.EvidenceType;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EvidenceTypeQuery {

    private final EvidenceTypeRepository evidenceTypeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<EvidenceType> evidenceTypes() {
        return support.allSortedByUri(evidenceTypeRepository, EvidenceType::getUri);
    }

    @QueryMapping
    public EvidenceType evidenceTypeByCode(@Argument String code) {
        return support.byCode(evidenceTypeRepository::findByCode, code);
    }
}
