package no.fdk.referencedata.apistatus;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-status")
@Slf4j
public class ApiStatusController {

    private ApiStatusService apiStatusService;

    @Autowired
    public ApiStatusController(ApiStatusService apiStatusService) {
        this.apiStatusService = apiStatusService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<ApiStatuses> getApiStatuses() {
        return ResponseEntity.ok(ApiStatuses.builder()
                .apiStatuses(apiStatusService.getAll())
                .build());
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<ApiStatus> getApiStatus(@PathVariable("code") final String code) {
        return ResponseEntity.of(apiStatusService.getByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public String getApiStatusesRDF() {
        return apiStatusService.getRdf(RDFFormat.TURTLE);
    }

}
