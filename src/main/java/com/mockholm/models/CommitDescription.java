package com.mockholm.models;

import com.mockholm.config.BranchAction;

/**
 * Represents a structured interpretation of a commit message,
 * typically used to describe automated or high-level Git actions tied to branch operations.
 * Includes the action performed, the branch involved, and an optional descriptive message.
 */
public class CommitDescription {

    private BranchAction action;
    private String branchName;
    private String message;

    private CommitDescription() {}

    /**
     * Returns the action associated with this commit description.
     *
     * @return the branch action (e.g., START, FINISH)
     */
    public BranchAction getAction() {
        return action;
    }

    /**
     * Sets the branch action for this description.
     *
     * @param action the action to assign
     */
    public void setAction(BranchAction action) {
        this.action = action;
    }

    /**
     * Returns the name of the branch involved in this commit.
     *
     * @return the branch name
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Sets the branch name involved in this commit.
     *
     * @param branchName the branch name to assign
     */
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    /**
     * Returns the descriptive message tied to this commit.
     *
     * @return the commit message, or {@code null} if not set
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the descriptive message for this commit.
     *
     * @param message the message to assign
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Produces a human-readable summary of the commit description,
     * formatted like: "Start work on feature/new-thing: Add first draft"
     *
     * @return the formatted description string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (action != null) {
            sb.append(capitalize(action.getValue()));
        } else {
            sb.append("Perform");
        }

        if (branchName != null && !branchName.isBlank()) {
            sb.append(" work on ").append(branchName);
        }

        if (message != null && !message.isBlank()) {
            sb.append(": ").append(message);
        }

        return sb.toString();
    }

    /**
     * Parses a string into a {@link CommitDescription} assuming the format:
     * {@code "Action work on branch: message"}
     *
     * @param input the formatted commit string to parse
     * @return the parsed {@code CommitDescription}
     * @throws IllegalArgumentException if the input is invalid or action is unrecognized
     */
    public static CommitDescription parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input string is empty");
        }

        String[] parts = input.split(": ", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid format. Expected 'Action work on branch: message'");
        }

        String header = parts[0].trim();
        String message = parts[1].trim();

        BranchAction action = null;
        String branchName = null;

        for (BranchAction a : BranchAction.values()) {
            String prefix = capitalize(a.getValue()) + " work on ";
            if (header.startsWith(prefix)) {
                action = a;
                branchName = header.substring(prefix.length()).trim();
                break;
            }
        }

        if (action == null) {
            throw new IllegalArgumentException("Unrecognized action in: " + header);
        }

        return new Builder()
                .action(action)
                .branchName(branchName)
                .message(message)
                .build();
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param input the string to capitalize
     * @return capitalized version of the input
     */
    private static String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * Fluent builder for constructing {@link CommitDescription} instances.
     */
    public static class Builder {
        private final CommitDescription description = new CommitDescription();

        /**
         * Sets the branch action.
         *
         * @param action the action to assign
         * @return this builder
         */
        public Builder action(BranchAction action) {
            description.setAction(action);
            return this;
        }

        /**
         * Sets the branch name.
         *
         * @param branchName the branch name
         * @return this builder
         */
        public Builder branchName(String branchName) {
            description.setBranchName(branchName);
            return this;
        }

        /**
         * Sets the commit message.
         *
         * @param message the commit message
         * @return this builder
         */
        public Builder message(String message) {
            description.setMessage(message);
            return this;
        }

        /**
         * Finalizes and returns the {@link CommitDescription}.
         *
         * @return the constructed description instance
         */
        public CommitDescription build() {
            return description;
        }
    }
}