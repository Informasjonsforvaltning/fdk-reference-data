package no.fdk.referencedata.i18n;

public enum Language {
    ENGLISH("en"),
    NORWEGIAN("no"),
    NORWEGIAN_NYNORSK("nn"),
    NORWEGIAN_BOKMAAL("nb");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String code() {
        return this.code;
    }
}
