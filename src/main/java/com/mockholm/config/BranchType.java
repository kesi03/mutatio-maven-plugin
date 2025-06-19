package com.mockholm.config;

/**
 * Represents the various types of Git branches commonly used in version control workflows.
 */
public enum BranchType {

    /** Archived or deprecated branches no longer in active development. */
    ARCHIVE("archive"),

    /** A branch used for fixing bugs, distinct from general fixes. */
    BUGFIX("bugfix"),

    /** Branches related to build process changes or fixes. */
    BUILD("build"),

    /** Branches for chores like dependency updates, formatting, or maintenance. */
    CHORE("chore"),

    /** Branches related to continuous integration configuration. */
    CI("ci"),

    /** Branches used for general code organization or utilities. */
    CODE("code"),

    /** Ongoing development branches, typically shared and unstable. */
    DEVELOPMENT("development"),

    /** Branches containing documentation-related updates. */
    DOCS("docs"),

    /** Branches for experimental or spike code that may be temporary. */
    EXPERIMENT("experiment"),

    /** Branches implementing new features. */
    FEATURE("feat"),

    /** General bug fixes not classified under bugfix or hotfix. */
    FIX("fix"),

    /** Urgent production fixes made directly on a release branch. */
    HOTFIX("hotfix"),

    /** Branches for general improvements or minor enhancements. */
    IMPROVEMENT("improvement"),

    /** Main production-ready branch in some workflows (alternative to master). */
    MAIN("main"),

    /** Legacy name for the default branch in many Git repositories. */
    MASTER("master"),

    /** Branches targeting performance enhancements. */
    PERF("perf"),

    /** Prototype branches for proofs of concept or throwaway work. */
    PROTOTYPE("prototype"),

    /** Branches dedicated to refactoring existing code without adding features or fixing bugs. */
    REFACTOR("refactor"),

    /** Branches for preparing and stabilizing release versions. */
    RELEASE("release"),

    /** Safe space branches used for individual experimentation or testing. */
    SANDBOX("sandbox"),

    /** Intermediate branches used for staging pre-production environments. */
    STAGING("staging"),

    /** Branches related to code styling, formatting, or aesthetic cleanup. */
    STYLE("style"),

    /** Branches containing test cases or updates to test frameworks. */
    TEST("test");

    private final String value;

    BranchType(String value) {
        this.value = value;
    }

    /**
     * Returns the string value associated with this branch type.
     *
     * @return the lowercase Git branch prefix
     */
    public String getValue() {
        return value;
    }

    public String getUppercaseValue() {
        return value.toUpperCase();
    }
}