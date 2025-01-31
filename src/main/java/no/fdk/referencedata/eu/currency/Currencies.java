package no.fdk.referencedata.eu.currency;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Currencies {
    List<Currency> currencies;
}
