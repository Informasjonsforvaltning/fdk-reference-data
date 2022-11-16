package no.fdk.referencedata.adms.status;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adms/statuses")
@Slf4j
public class ADMSStatusController {

    private final ADMSStatusService admsStatusService;

    @Autowired
    public ADMSStatusController(ADMSStatusService admsStatusService) {
        this.admsStatusService = admsStatusService;
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<ADMSStatuses> getADMSStatuses() {
        return ResponseEntity.ok(ADMSStatuses.builder()
                .statuses(admsStatusService.getAll())
                .build());
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<ADMSStatus> getADMSStatus(@PathVariable("code") final String code) {
        return ResponseEntity.of(admsStatusService.getByCode(code));
    }

}
