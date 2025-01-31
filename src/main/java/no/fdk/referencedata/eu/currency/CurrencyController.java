package no.fdk.referencedata.eu.currency;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/eu/currencies")
@Slf4j
public class CurrencyController {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CurrencyService currencyService;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<Currencies> getCurrencies() {
        return ResponseEntity.ok(Currencies.builder().currencies(
                StreamSupport.stream(currencyRepository.findAll().spliterator(), false)
                        .sorted(Comparator.comparing(Currency::getUri))
                        .collect(Collectors.toList())).build());
    }

    @CrossOrigin
    @PostMapping
    public ResponseEntity<Void> updateCurrencies() {
        currencyService.harvestAndSave(true);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @GetMapping(path = "/{code}")
    public ResponseEntity<Currency> getCurrency(@PathVariable("code") String code) {
        return ResponseEntity.of(currencyRepository.findByCode(code));
    }

    @CrossOrigin
    @GetMapping(produces = "text/turtle")
    public ResponseEntity<String> getCurrenciesRDF() {
        return ResponseEntity.ok(currencyService.getRdf(RDFFormat.TURTLE));
    }
}
