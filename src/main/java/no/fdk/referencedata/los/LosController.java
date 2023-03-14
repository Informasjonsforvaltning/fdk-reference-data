package no.fdk.referencedata.los;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

import static java.net.URLDecoder.decode;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/los")
public class LosController {

    private final LosService losService;

    @CrossOrigin
    @GetMapping(path = "themes-and-words", produces = MediaType.APPLICATION_JSON_VALUE)
    public LosNodes getLosNodes(@RequestParam(value = "uris", required = false) List<String> uris) {
        return LosNodes.builder()
            .losNodes(uris != null ?
                    losService.getByURIs(uris.stream().map(s -> {
                        try {
                            return decode(s, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            log.error("Unable to decode uri " + s, e);
                            return s;
                        }
                    }).collect(Collectors.toList())) :
                    losService.getAll())
            .build();
    }

    @CrossOrigin
    @GetMapping(path = "themes-and-words", produces = "text/turtle")
    public String getLosRDF() {
        return losService.getRdf(RDFFormat.TURTLE);
    }
}
