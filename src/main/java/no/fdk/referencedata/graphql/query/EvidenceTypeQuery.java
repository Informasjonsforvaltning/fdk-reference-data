package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.digdir.evidencetype.EvidenceType;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class EvidenceTypeQuery implements GraphQLQueryResolver {

    @Autowired
    private EvidenceTypeRepository evidenceTypeRepository;

    public List<EvidenceType> getEvidenceTypes() {
        return StreamSupport.stream(evidenceTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(EvidenceType::getUri))
                .collect(Collectors.toList());
    }

    public EvidenceType getEvidenceTypeByCode(String code) {
        return evidenceTypeRepository.findByCode(code).orElse(null);
    }
}
