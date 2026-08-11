package no.fdk.referencedata.digdir.roletype;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleTypeService implements HarvestableReferenceData {
    private final String dbSourceID = "role-types-source";

    private final RoleTypeHarvester roleTypeHarvester;

    private final RoleTypeRepository roleTypeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public RoleTypeService(
            RoleTypeHarvester roleTypeHarvester,
            RoleTypeRepository roleTypeRepository,
            ReferenceDataServiceSupport support) {
        this.roleTypeHarvester = roleTypeHarvester;
        this.roleTypeRepository = roleTypeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(roleTypeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(roleTypeHarvester, roleTypeRepository, dbSourceID, "role-type");
    }
}
