package no.fdk.referencedata.eu.mainactivity;

import no.fdk.referencedata.eu.GenericEuSkosHarvester;
import no.fdk.referencedata.eu.vocabulary.EUMainActivity;
import no.fdk.referencedata.rdf.SkosMapper;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class MainActivityHarvester extends GenericEuSkosHarvester<MainActivity> {

    @Override
    protected String schemaName() {
        return "main-activity";
    }

    @Override
    protected Resource scheme() {
        return EUMainActivity.SCHEME;
    }

    @Override
    protected String logName() {
        return "main-activity";
    }

    @Override
    protected MainActivity mapConcept(Resource mainActivity) {
        return MainActivity.builder()
                .uri(mainActivity.getURI())
                .code(extractCode(mainActivity))
                .label(SkosMapper.extractLabels(mainActivity))
                .build();
    }
}
