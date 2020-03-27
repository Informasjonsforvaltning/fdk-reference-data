package no.dcat.themes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.dcat.shared.DataTheme;
import no.dcat.shared.LocationUri;
import no.dcat.shared.SkosCode;
import no.dcat.shared.Types;
import no.dcat.themes.service.CodesService;
import no.dcat.themes.service.LosNode;
import no.dcat.themes.service.LosService;
import no.dcat.themes.service.ThemesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Scope("thread")
public class Controller {
    private final CodesService codesService;
    private final ThemesService themesService;
    private final LosService losService;

    @CrossOrigin
    @GetMapping(value = "/codes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<String> codeTypes() {
        return codesService.listCodes();
    }

    @CrossOrigin
    @GetMapping(value = "/codes/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<SkosCode> codes(@PathVariable Types type) {
        return codesService.getCodes(type);
    }

    @CrossOrigin
    @GetMapping(value = "/los", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<LosNode> getLosThemes(@RequestParam(value = "uris", required = false) List<String> uris) {
        return uris != null ? losService.getByURIs(uris) : losService.getAll();
    }

    @CrossOrigin
    @RequestMapping(value = "/loscodesbyid", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public LosNode getLosNode(String id) {
        try {
            URI u = new URI(id);
            return LosService.getByURI(u);
        } catch (URISyntaxException use) {
            log.debug("Request for LOS by URI failed. URI " + id);
        }
        return null;
    }

    @CrossOrigin
    @RequestMapping(value = "/loscodes/hasLosTheme", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Boolean hasLosTheme(String[] themes) {
        if (themes == null || themes.length == 0) {
            return false;
        }
        List<String> themesList = Arrays.asList(themes);
        return losService.hasLosThemes(themesList);
    }

    @CrossOrigin
    @RequestMapping(value = "/loscodes/expandLosTheme", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String[] expandLosThemes(String[] themes) {
        if (themes == null || themes.length == 0) {
            return null;
        }
        List<String> themesList = Arrays.asList(themes);
        List<String> expanded = losService.expandLosThemes(themesList);
        return expanded.toArray(new String[0]);
    }

    @CrossOrigin
    @RequestMapping(value = "/loscodes/expandLosThemeByPaths", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String[] expandLosThemesByPaths(String[] themes) {
        if (themes == null || themes.length == 0) {
            return null;
        }
        List<String> themesList = Arrays.asList(themes);
        List<String> expanded = losService.expandLosThemesByPaths(themesList);
        return expanded.toArray(new String[0]);
    }

    @CrossOrigin
    @RequestMapping(value = "/themes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<DataTheme> themes() {
        return themesService.getThemes();
    }

    @PreAuthorize("hasAuthority('INTERNAL_CALL')")
    @CrossOrigin
    @RequestMapping(value = "/locations", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public SkosCode putLocation(@RequestBody LocationUri resource) throws MalformedURLException {
        log.info("register new location: {}", resource.getUri());
        try {
            return codesService.addLocation(resource.getUri());
        } catch (Exception e) {
            log.error("Unable to find location with URI <{}>. Reason {}", resource.getUri(), e.getMessage());
            throw e;
        }
    }
}
