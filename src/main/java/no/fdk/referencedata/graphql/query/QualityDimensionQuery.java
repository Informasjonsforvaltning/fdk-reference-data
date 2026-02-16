package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.digdir.qualitydimension.QualityDimension;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimensionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class QualityDimensionQuery {

    @Autowired
    private QualityDimensionRepository qualityDimensionRepository;

    @QueryMapping
    public List<QualityDimension> qualityDimensions() {
        return StreamSupport.stream(qualityDimensionRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(QualityDimension::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public QualityDimension qualityDimensionByCode(@Argument String code) {
        return qualityDimensionRepository.findByCode(code).orElse(null);
    }
}
