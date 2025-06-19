package com.mockholm.config;

public enum VersionIdentifier {
    SNAPSHOT("SNAPSHOT"),
    BETA("BETA"),
    ALPHA("ALPHA"),
    RC("RC"),
    NONE("");


    private final String value;

    VersionIdentifier(String value){
        this.value=value;
    }

    public String getValue() {
        return value;
    }
}
