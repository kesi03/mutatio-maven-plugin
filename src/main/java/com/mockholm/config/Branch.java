package com.mockholm.config;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Represents a source control branch configuration, including its name, description,
 * and type. Used for defining project versioning and workflow structure.
 */
public class Branch {

    /** Name of the branch, such as "feature/login" or "release/v1.0". */
    @Parameter
    private String name;

    /** Optional description for the branch, providing additional context. */
    @Parameter
    private String description;

    /** Type of the branch (e.g. FEATURE, RELEASE, HOTFIX), defined by {@link BranchType}. */
    @Parameter
    private BranchType branchType;

    /**
     * Returns the name of the branch.
     *
     * @return the branch name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the branch name and returns the current instance (builder-style).
     *
     * @param name the branch name
     * @return the updated Branch instance
     */
    public Branch withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the branch name.
     *
     * @param name the branch name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the branch description.
     *
     * @return the description text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the branch description and returns the current instance (builder-style).
     *
     * @param description the description text
     * @return the updated Branch instance
     */
    public Branch withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the branch description.
     *
     * @param description the description text
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the type of the branch.
     *
     * @return the {@link BranchType}
     */
    public BranchType getBranchType() {
        return branchType;
    }

    // Optional builder method for setting branchType could be included:
    // /**
    //  * Sets the branch type and returns the current instance (builder-style).
    //  *
    //  * @param branchType the {@link BranchType}
    //  * @return the updated Branch instance
    //  */
    // public Branch withBranchType(BranchType branchType) {
    //     this.branchType = branchType;
    //     return this;
    // }

    /**
     * Sets the type of the branch.
     *
     * @param branchType the {@link BranchType}
     */
    public void setBranchType(BranchType branchType) {
        this.branchType = branchType;
    }

    /**
     * Constructs the full branch name, formatted as "type/name".
     * If either part is missing, defaults to "unknown" or "unnamed".
     *
     * @return a full identifier for the branch
     */
    public String getBranchFullName() {
        return (branchType != null ? branchType.getValue() : "unknown") +
                "/" +
                (name != null ? name : "unnamed");
    }
}