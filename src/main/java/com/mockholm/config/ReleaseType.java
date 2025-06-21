package com.mockholm.config;

/**
 * Enum representing the type of version change according to Semantic Versioning.
 * Used to classify whether a release introduces breaking changes, new features,
 * or backwards-compatible fixes.
 */
public enum ReleaseType {
    /**
     * Indicates a major release that introduces breaking changes.
     */
    MAJOR,

    /**
     * Indicates a minor release that adds backwards-compatible functionality.
     */
    MINOR,

    /**
     * Indicates a patch release that includes backwards-compatible bug fixes.
     */
    PATCH
}