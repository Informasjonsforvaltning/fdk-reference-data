package no.fdk.referencedata.eu.accessright;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AccessRights {
    List<AccessRight> accessRights;
}
