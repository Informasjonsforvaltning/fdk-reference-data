package no.fdk.referencedata.datatheme;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

public interface DataThemeHarvester {
    Resource getDataThemesSource();
    String getVersion() throws Exception;
    Flux<DataTheme> harvestDataThemes();
}
