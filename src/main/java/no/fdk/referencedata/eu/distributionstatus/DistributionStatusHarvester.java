package no.fdk.referencedata.eu.distributionstatus;

import no.fdk.referencedata.eu.GenericEuSkosHarvester;
import no.fdk.referencedata.eu.vocabulary.EUDistributionStatus;
import no.fdk.referencedata.rdf.SkosMapper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DistributionStatusHarvester extends GenericEuSkosHarvester<DistributionStatus> {

    private final Map<String, Map<String, String>> missingTranslations = Map.ofEntries(
            Map.entry(EUDistributionStatus.getURI() + "/ARCHIVE", Map.of(
                    "nb", "arkivert",
                    "nn", "arkivert"
            )),
            Map.entry(EUDistributionStatus.getURI() + "/ONGOING", Map.of(
                    "nb", "pågående",
                    "nn", "pågåande"
            )),
            Map.entry(EUDistributionStatus.getURI() + "/PLANNED", Map.of(
                    "nb", "planlagt",
                    "nn", "planlagt"
            )),
            Map.entry(EUDistributionStatus.getURI() + "/REQUIRED", Map.of(
                    "nb", "påkrevd",
                    "nn", "påkravd"
            ))
    );

    @Override
    protected String schemaName() {
        return "distribution-status";
    }

    @Override
    protected Resource scheme() {
        return EUDistributionStatus.SCHEME;
    }

    @Override
    protected String logName() {
        return "distribution statuses";
    }

    @Override
    protected Model translate(Model model) {
        Model translated = ModelFactory.createDefaultModel();
        model.listStatements().forEach(translated::add);

        for (String subject : missingTranslations.keySet()) {
            Resource subjectResource = model.getResource(subject);
            Map<String, String> subjectTranslations = missingTranslations.get(subject);
            for (Map.Entry<String, String> entry : subjectTranslations.entrySet()) {
                translated.add(
                        subjectResource,
                        SKOS.prefLabel,
                        entry.getValue(),
                        entry.getKey()
                );
            }
        }

        updateModel(translated);
        return translated;
    }

    @Override
    protected DistributionStatus mapConcept(Resource distributionStatus) {
        return DistributionStatus.builder()
                .uri(distributionStatus.getURI())
                .code(extractCode(distributionStatus))
                .label(SkosMapper.extractLabels(distributionStatus))
                .startUse(extractStartUse(distributionStatus))
                .build();
    }
}
