package no.fdk.referencedata.digdir.roletype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoleTypes {
    List<RoleType> roleTypes;
}
