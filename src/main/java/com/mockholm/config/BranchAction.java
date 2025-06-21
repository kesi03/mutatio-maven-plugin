package com.mockholm.config;

/**
 * Enum representing high-level actions that can be performed on a Git branch.
 * These actions are typically used to describe intent in version control workflows,
 * such as starting, finishing, merging, or updating a branch.
 */
public enum BranchAction {
    /**
     * Indicates the start of work on a new branch.
     */
    START("start"),

    /**
     * Indicates the completion of work on a branch.
     */
    FINISH("finish"),

    /**
     * Indicates a merge operation involving the branch.
     */
    MERGE("merge"),

    /**
     * Indicates an update to an existing branch.
     */
    UPDATE("update");

    private final String value;

    /**
     * Constructs a {@code BranchAction} with its associated string value.
     *
     * @param value the string representation of the branch action
     */
    BranchAction(String value) {
        this.value = value;
    }

    /**
     * Returns the string value associated with this branch action.
     *
     * @return the action's string representation (e.g., "start", "merge")
     */
    public String getValue() {
        return value;
    }
}