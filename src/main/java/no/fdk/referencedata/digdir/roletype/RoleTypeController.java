package no.fdk.referencedata.digdir.roletype;

import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/digdir/role-types")
@Slf4j
public class RoleTypeController {

    @Autowired
    private RoleTypeRepository roleTypeRepository;

    @Autowired
    private RoleTypeService roleTypeService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<RoleTypes> getRoleTypes() {
        return ResponseEntity.ok(RoleTypes.builder().roleTypes(
                StreamSupport.stream(roleTypeRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(RoleType::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateRoleTypes() {
        roleTypeService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<RoleType> getRoleType(@PathVariable("code") String code) {
        return ResponseEntity.of(roleTypeRepository.findByCode(code));
    }
}
