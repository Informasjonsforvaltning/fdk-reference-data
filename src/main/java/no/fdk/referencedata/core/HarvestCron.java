package no.fdk.referencedata.core;

/**
 * Cron expressions for scheduled reference data harvests.
 */
public final class HarvestCron {

    // Digdir
    public static final String CRON_CONCEPT_SUBJECT = "0 45 * * * ?";
    public static final String CRON_EVIDENCE_TYPE = "0 0 1 1 * ?";
    public static final String CRON_SERVICE_CHANNEL_TYPE = "0 10 1 1 * ?";
    public static final String CRON_ROLE_TYPE = "0 20 1 1 * ?";
    public static final String CRON_AUDIENCE_TYPE = "0 50 3 1 * ?";
    public static final String CRON_RELATIONSHIP_WITH_SOURCE_TYPE = "0 10 4 1 * ?";
    public static final String CRON_QUALITY_DIMENSION = "0 10 5 1 * ?";
    public static final String CRON_LEGAL_RESOURCE_TYPE = "0 15 5 1 * ?";

    // EU
    public static final String CRON_ACCESS_RIGHT = "0 30 1 1 * ?";
    public static final String CRON_FILE_TYPE = "0 50 1 1 * ?";
    public static final String CRON_DATA_THEME = "0 0 2 1 * ?";
    public static final String CRON_EUROVOC = "0 10 2 1 * ?";
    public static final String CRON_FREQUENCY = "0 20 2 1 * ?";
    public static final String CRON_DISTRIBUTION_STATUS = "0 25 2 1 * ?";
    public static final String CRON_DISTRIBUTION_TYPE = "0 30 2 1 * ?";
    public static final String CRON_DATASET_TYPE = "0 35 2 1 * ?";
    public static final String CRON_MAIN_ACTIVITY = "0 40 2 1 * ?";
    public static final String CRON_CONCEPT_STATUS = "0 40 3 1 * ?";
    public static final String CRON_PLANNED_AVAILABILITY = "0 20 4 1 * ?";
    public static final String CRON_CURRENCY = "0 30 4 1 * ?";
    public static final String CRON_LICENCE = "0 40 4 1 * ?";
    public static final String CRON_HIGH_VALUE_CATEGORY = "0 5 5 1 * ?";
    public static final String CRON_CONTINENT = "0 20 5 1 * ?";
    public static final String CRON_COUNTRY = "0 25 5 1 * ?";
    public static final String CRON_LANGUAGE = "0 35 5 1 * ?";

    // Mobility
    public static final String CRON_MOBILITY_THEME = "0 50 4 1 * ?";
    public static final String CRON_MOBILITY_CONDITION = "0 55 4 1 * ?";
    public static final String CRON_MOBILITY_DATA_STANDARD = "0 0 5 1 * ?";

    // Other
    public static final String CRON_MEDIA_TYPE = "0 40 1 1 * ?";
    public static final String CRON_LOS = "0 50 2 1 * ?";
    public static final String CRON_FYLKE_ORGANISASJON = "0 10 3 1 * ?";
    public static final String CRON_KOMMUNE_ORGANISASJON = "0 20 3 1 * ?";
    public static final String CRON_ENHET = "0 0 4 1 * ?";
    public static final String CRON_GEONAMES = "0 30 5 1 * ?";

    private HarvestCron() {
    }
}
