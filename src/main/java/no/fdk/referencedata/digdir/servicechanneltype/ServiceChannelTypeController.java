package no.fdk.referencedata.digdir.servicechanneltype;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/digdir/service-channel-types")
@Slf4j
public class ServiceChannelTypeController {

    @Autowired
    private ServiceChannelTypeRepository serviceChannelTypeRepository;

    @Autowired
    private ServiceChannelTypeService serviceChannelTypeService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<ServiceChannelTypes> getServiceChannelTypes() {
        return ResponseEntity.ok(ServiceChannelTypes.builder().serviceChannelTypes(
                StreamSupport.stream(serviceChannelTypeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(ServiceChannelType::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateServiceChannelTypes() {
        serviceChannelTypeService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<ServiceChannelType> getServiceChannelType(@PathVariable("code") String code) {
        return ResponseEntity.of(serviceChannelTypeRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getServiceChannelTypesRDF() {
        return ResponseEntity.ok(serviceChannelTypeService.getRdf(RDFFormat.TURTLE));
    }
}
