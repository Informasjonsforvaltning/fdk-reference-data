package no.fdk.referencedata.eu.licence;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Licences {
    List<Licence> licences;
}
