package com.mockholm.utils;


import java.util.regex.Pattern;

public class SemanticVersion {
    private static final Pattern SEMVER_PATTERN = Pattern.compile(
            "^(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-([0-9A-Za-z.-]+))?(?:\\+([0-9A-Za-z.-]+))?$"
    );

    private final int major;
    private final int minor;
    private final int patch;
    private final String preRelease;
    private final String build;

    public SemanticVersion(int major, int minor, int patch, String preRelease, String build) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preRelease = preRelease;
        this.build = build;
    }

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(major).append(".").append(minor).append(".").append(patch);
        if (preRelease != null) sb.append("-").append(preRelease);
        if (build != null) sb.append("+").append(build);
        return sb.toString();
    }

    public int getMajor() { return major; }
    public int getMinor() { return minor; }
    public int getPatch() { return patch; }
    public String getPreRelease() { return preRelease; }
    public String getBuild() { return build; }
}


