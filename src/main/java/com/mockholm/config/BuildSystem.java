package com.mockholm.config;

/**
 * Defines supported Continuous Integration/Continuous Deployment (CI/CD) systems
 * that the build logic can integrate with or detect.
 */
public enum BuildSystem {

    /** Represents a TeamCity build environment. */
    TEAM_CITY,

    /** Represents an Azure DevOps build environment. */
    AZURE_DEVOPS,

    /** Represents a GitHub Actions workflow environment. */
    GITHUB_ACTIONS,

    /** Represents a Jenkins CI build environment. */
    JENKINS,

    /** Indicates an unknown or unsupported build system. */
    UNKNOWN;
}