package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimension;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimensionRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class QualityDimensionQuery {

    private final QualityDimensionRepository qualityDimensionRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<QualityDimension> qualityDimensions() {
        return support.allSortedByUri(qualityDimensionRepository, QualityDimension::getUri);
    }

    @QueryMapping
    public QualityDimension qualityDimensionByCode(@Argument String code) {
        return support.byCode(qualityDimensionRepository::findByCode, code);
    }
}
