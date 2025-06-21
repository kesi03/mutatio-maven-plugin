package com.mockholm.utils;


import java.util.regex.Pattern;

/**
 * A representation of a semantic version string, parsed into its structured components:
 * major, minor, patch, pre-release, and build metadata.
 * <p>
 * This class supports parsing and rendering of semantic versions compliant with
 * the Semantic Versioning 2.0.0 specification.
 */
public class SemanticVersion {
    /**
     * Regular expression pattern for parsing semantic version components.
     * Captures major, minor, patch, optional pre-release, and build segments.
     */
    private static final Pattern SEMVER_PATTERN = Pattern.compile(
            "^(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-([0-9A-Za-z.-]+))?(?:\\+([0-9A-Za-z.-]+))?$"
    );

    /**
     * major the major version number (must be non-negative)
     */
    private final int major;
    /**
     * minor the minor version number (must be non-negative)
     */
    private final int minor;
    /**
     * patch the patch version number (defaults to 0 if absent)
     */
    private final int patch;
    /**
     * preRelease optional pre-release label (e.g., "alpha", "rc.1")
     */
    private final String preRelease;
    /**
     * build optional build metadata (e.g., "build.2024")
     */
    private final String build;

    /**
     * Constructs a new SemanticVersion instance with the given components.
     *
     * @param major the major version number (must be non-negative)
     * @param minor the minor version number (must be non-negative)
     * @param patch the patch version number (defaults to 0 if absent)
     * @param preRelease optional pre-release label (e.g., "alpha", "rc.1")
     * @param build optional build metadata (e.g., "build.2024")
     */
    public SemanticVersion(int major, int minor, int patch, String preRelease, String build) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preRelease = preRelease;
        this.build = build;
    }

    /**
     * Parses a semantic version string into a {@link SemanticVersion} instance.
     * The input must conform to the format: MAJOR.MINOR[.PATCH][-PRERELEASE][+BUILD]
     *
     * @param version the version string to parse
     * @return a structured {@code SemanticVersion} object
     * @throws IllegalArgumentException if the input does not match the expected format
     */
    public static SemanticVersion parse(String version) {
        var matcher = SEMVER_PATTERN.matcher(version);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid semantic version: " + version);
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0; // Default patch to 0
        String preRelease = matcher.group(4);
        String build = matcher.group(5);

        return new SemanticVersion(major, minor, patch, preRelease, build);
    }

    /**
     * Returns the full semantic version string assembled from its components.
     *
     * @return the version string (e.g., "1.2.3-alpha+exp.sha.5114f85")
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(major).append(".").append(minor).append(".").append(patch);
        if (preRelease != null && !preRelease.isEmpty()) {
            sb.append("-").append(preRelease);
        }
        if (build != null && !build.isEmpty()) {
            sb.append("+").append(build);
        }
        return sb.toString();
    }

    /**
     * Returns the major version number.
     *
     * @return the major component
     */
    public int getMajor() { return major; }

    /**
     * Returns the minor version number.
     *
     * @return the minor component
     */
    public int getMinor() { return minor; }

    /**
     * Returns the patch version number.
     *
     * @return the patch component
     */
    public int getPatch() { return patch; }

    /**
     * Returns the pre-release identifier, if any.
     *
     * @return the pre-release string or {@code null} if absent
     */
    public String getPreRelease() { return preRelease; }

    /**
     * Returns the build metadata string, if any.
     *
     * @return the build string or {@code null} if absent
     */
    public String getBuild() { return build; }
}


