package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.digdir.evidencetype.EvidenceType;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class EvidenceTypeQuery {

    @Autowired
    private EvidenceTypeRepository evidenceTypeRepository;

    @QueryMapping
    public List<EvidenceType> evidenceTypes() {
        return StreamSupport.stream(evidenceTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(EvidenceType::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public EvidenceType evidenceTypeByCode(@Argument String code) {
        return evidenceTypeRepository.findByCode(code).orElse(null);
    }
}
