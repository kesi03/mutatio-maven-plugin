package com.mockholm.config;

/**
 * Used to determine which kind of release notes are wanted
 */
public enum ReleaseNotesType {
    /** Standard release notes sorted by latest first*/
    STANDARD,
    /** Release notes sorted by latest first and by category*/
    CATEGORY,
    /** Release notes sorted by latest first and by {@link BranchType}*/
    BRANCH
}
