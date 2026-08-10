package no.fdk.referencedata;

import no.fdk.referencedata.ApplicationSettings;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeHarvester;
import no.fdk.referencedata.digdir.conceptsubjects.ConceptSubjectHarvester;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeHarvester;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceTypeHarvester;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimensionHarvester;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeHarvester;
import no.fdk.referencedata.digdir.roletype.RoleTypeHarvester;
import no.fdk.referencedata.digdir.servicechanneltype.ServiceChannelTypeHarvester;
import no.fdk.referencedata.eu.accessright.AccessRightHarvester;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusHarvester;
import no.fdk.referencedata.eu.continent.ContinentHarvester;
import no.fdk.referencedata.eu.country.CountryHarvester;
import no.fdk.referencedata.eu.currency.CurrencyHarvester;
import no.fdk.referencedata.eu.datasettype.DatasetTypeHarvester;
import no.fdk.referencedata.eu.datatheme.DataThemeHarvester;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatusHarvester;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeHarvester;
import no.fdk.referencedata.eu.eurovoc.EuroVocHarvester;
import no.fdk.referencedata.eu.filetype.FileTypeHarvester;
import no.fdk.referencedata.eu.frequency.FrequencyHarvester;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategoriesHarvester;
import no.fdk.referencedata.eu.language.LanguageHarvester;
import no.fdk.referencedata.eu.licence.LicenceHarvester;
import no.fdk.referencedata.eu.mainactivity.MainActivityHarvester;
import no.fdk.referencedata.eu.plannedavailability.PlannedAvailabilityHarvester;
import no.fdk.referencedata.mobility.conditions.MobilityConditionHarvester;
import no.fdk.referencedata.mobility.datastandard.MobilityDataStandardHarvester;
import no.fdk.referencedata.mobility.theme.MobilityThemeHarvester;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/** Factory for classpath-backed harvesters used in tests. */
public final class LocalHarvesters {

    private LocalHarvesters() {}

    private static Resource classpath(String name) {
        return new ClassPathResource(name);
    }

    public static AccessRightHarvester accessRight() {
        return new AccessRightHarvester() {
            @Override
            public Resource getSource() {
                return classpath("access-right-sparql-result.ttl");
            }
        };
    }

    public static ConceptStatusHarvester conceptStatus() {
        return new ConceptStatusHarvester() {
            @Override
            public Resource getSource() {
                return classpath("concept-status.ttl");
            }
        };
    }

    public static ContinentHarvester continent() {
        return new ContinentHarvester() {
            @Override
            public Resource getSource() {
                return classpath("continent-sparql-result.ttl");
            }
        };
    }

    public static CountryHarvester country() {
        return new CountryHarvester() {
            @Override
            public Resource getSource() {
                return classpath("country-sparql-result.ttl");
            }
        };
    }

    public static CurrencyHarvester currency() {
        return new CurrencyHarvester() {
            @Override
            public Resource getSource() {
                return classpath("currency-sparql-result.ttl");
            }
        };
    }

    public static DatasetTypeHarvester datasetType() {
        return new DatasetTypeHarvester() {
            @Override
            public Resource getSource() {
                return classpath("dataset-types-sparql-result.ttl");
            }
        };
    }

    public static DataThemeHarvester dataTheme() {
        return new DataThemeHarvester() {
            @Override
            public Resource getSource() {
                return classpath("data-theme-sparql-result.ttl");
            }
        };
    }

    public static DistributionStatusHarvester distributionStatus() {
        return new DistributionStatusHarvester() {
            @Override
            public Resource getSource() {
                return classpath("distribution-status-sparql-result.ttl");
            }
        };
    }

    public static DistributionTypeHarvester distributionType() {
        return new DistributionTypeHarvester() {
            @Override
            public Resource getSource() {
                return classpath("distribution-types-sparql-result.ttl");
            }
        };
    }

    public static EuroVocHarvester euroVoc() {
        return new EuroVocHarvester() {
            @Override
            public Resource getSource() {
                return classpath("eurovoc-sparql-result.ttl");
            }
        };
    }

    public static FileTypeHarvester fileType() {
        return new FileTypeHarvester() {
            @Override
            public Resource getSource() {
                return classpath("filetypes-sparql-result.ttl");
            }
        };
    }

    public static FrequencyHarvester frequency() {
        return new FrequencyHarvester() {
            @Override
            public Resource getSource() {
                return classpath("frequencies-sparql-result.ttl");
            }
        };
    }

    public static HighValueCategoriesHarvester highValueCategory() {
        return new HighValueCategoriesHarvester() {
            @Override
            public Resource getSource() {
                return classpath("high-value-categories.ttl");
            }
        };
    }

    public static LanguageHarvester language() {
        return new LanguageHarvester() {
            @Override
            public Resource getSource() {
                return classpath("language-sparql-result.ttl");
            }
        };
    }

    public static LicenceHarvester licence() {
        return new LicenceHarvester() {
            @Override
            public Resource getSource() {
                return classpath("licences-sparql-result.ttl");
            }
        };
    }

    public static MainActivityHarvester mainActivity() {
        return new MainActivityHarvester() {
            @Override
            public Resource getSource() {
                return classpath("main-activity-sparql-result.ttl");
            }
        };
    }

    public static PlannedAvailabilityHarvester plannedAvailability() {
        return new PlannedAvailabilityHarvester() {
            @Override
            public Resource getSource() {
                return classpath("planned-availability-sparql-result.ttl");
            }
        };
    }

    public static AudienceTypeHarvester audienceType() {
        return new AudienceTypeHarvester() {
            @Override
            public Resource getSource(final String path) {
                return classpath("audience-type.ttl");
            }
        };
    }

    public static EvidenceTypeHarvester evidenceType() {
        return new EvidenceTypeHarvester() {
            @Override
            public Resource getSource(final String path) {
                return classpath("evidence-type.ttl");
            }
        };
    }

    public static LegalResourceTypeHarvester legalResourceType() {
        return new LegalResourceTypeHarvester() {
            @Override
            public Resource getSource(final String path) {
                return classpath("legal-resource-type.ttl");
            }
        };
    }

    public static QualityDimensionHarvester qualityDimension() {
        return new QualityDimensionHarvester() {
            @Override
            public Resource getSource(final String path) {
                return classpath("quality-dimension.ttl");
            }
        };
    }

    public static RelationshipWithSourceTypeHarvester relationshipWithSourceType() {
        return new RelationshipWithSourceTypeHarvester() {
            @Override
            public Resource getSource(final String path) {
                return classpath("relationship-with-source-type.ttl");
            }
        };
    }

    public static RoleTypeHarvester roleType() {
        return new RoleTypeHarvester() {
            @Override
            public Resource getSource(final String path) {
                return classpath("role-type.ttl");
            }
        };
    }

    public static ServiceChannelTypeHarvester serviceChannelType() {
        return new ServiceChannelTypeHarvester() {
            @Override
            public Resource getSource(final String path) {
                return classpath("service-channel-type.ttl");
            }
        };
    }

    public static MobilityThemeHarvester mobilityTheme() {
        return new MobilityThemeHarvester() {
            @Override
            public Resource getSource(final String path) {
                return classpath("mobility-themes.ttl");
            }
        };
    }

    public static MobilityDataStandardHarvester mobilityDataStandard() {
        return new MobilityDataStandardHarvester() {
            @Override
            public Resource getSource(final String path) {
                return classpath("mobility-data-standards.ttl");
            }
        };
    }

    public static MobilityConditionHarvester mobilityCondition() {
        return new MobilityConditionHarvester() {
            @Override
            public Resource getSource(final String path) {
                return classpath("mobility-conditions.ttl");
            }
        };
    }

    public static ConceptSubjectHarvester conceptSubject(ApplicationSettings applicationSettings) {
        return new ConceptSubjectHarvester(applicationSettings) {
            @Override
            public Resource getSource() {
                return classpath("concept-subjects.ttl");
            }
        };
    }

}
