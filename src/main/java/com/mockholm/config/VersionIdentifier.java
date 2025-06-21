package com.mockholm.config;

/**
 * Enum representing different types of version identifiers
 * commonly used in semantic versioning and release lifecycles.
 * <p>
 * Examples include snapshot builds, pre-releases like beta or alpha versions,
 * release candidates (RC), or no version tag at all.
 */
public enum VersionIdentifier {
    /**
     * Indicates a snapshot version, typically used for ongoing or in-development builds.
     */
    SNAPSHOT("SNAPSHOT"),

    /**
     * Indicates a beta version, usually for feature-complete testing before release.
     */
    BETA("BETA"),

    /**
     * Indicates an alpha version, often used for early testing and unstable features.
     */
    ALPHA("ALPHA"),

    /**
     * Indicates a release candidate — a build close to final release status.
     */
    RC("RC"),

    /**
     * Indicates no version identifier or a stable release with no suffix.
     */
    NONE("");

    private final String value;

    /**
     * Constructs a VersionIdentifier with its corresponding string value.
     *
     * @param value the identifier string (e.g., "SNAPSHOT", "BETA")
     */
    VersionIdentifier(String value) {
        this.value = value;
    }

    /**
     * Returns the string value associated with this version identifier.
     *
     * @return the identifier's string representation
     */
    public String getValue() {
        return value;
    }
}