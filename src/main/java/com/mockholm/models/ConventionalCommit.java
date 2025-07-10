package com.mockholm.models;

import com.mockholm.config.BranchType;

/**
 * Represents a Conventional Commit, following the common commit message format:
 * &lt;type&gt;(scope): &lt;description&gt;
 *
 * Includes extended fields for breaking changes, detailed body content, and footers.
 */
public class ConventionalCommit {

    /** The type of commit (e.g. feat, fix), mapped from {@link BranchType}. */
    private BranchType type;

    /** Scope of the change (e.g. module, component). */
    private String scope;

    /** Short, imperative summary describing the change. */
    private String description;

    /** Indicates whether this commit introduces breaking changes. */
    private boolean isBreaking;

    /** Optional body that provides more detailed context. */
    private String body;

    /** Optional footer section, often used for metadata or references. */
    private String footer;

    /**
     * Private constructor to enforce the use of the Builder pattern.
     */
    private ConventionalCommit() {}

    /**
     * Gets the commit type.
     *
     * @return the {@link BranchType}
     */
    public BranchType getType() { return type; }

    /**
     * Sets the commit type.
     *
     * @param type the {@link BranchType}
     */
    public void setType(BranchType type) { this.type = type; }

    /**
     * Gets the scope of the commit.
     *
     * @return the scope string
     */
    public String getScope() { return scope; }

    /**
     * Sets the scope of the commit.
     *
     * @param scope the scope string
     */
    public void setScope(String scope) { this.scope = scope; }

    /**
     * Gets the short description of the commit.
     *
     * @return the description string
     */
    public String getDescription() { return description; }

    /**
     * Sets the short description of the commit.
     *
     * @param description the description string
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Returns whether the commit contains breaking changes.
     *
     * @return true if breaking changes are present
     */
    public boolean isBreaking() { return isBreaking; }

    /**
     * Sets the breaking change flag.
     *
     * @param breaking true if commit introduces breaking changes
     */
    public void setBreaking(boolean breaking) { isBreaking = breaking; }

    /**
     * Gets the body of the commit.
     *
     * @return the body string
     */
    public String getBody() { return body; }

    /**
     * Sets the body of the commit.
     *
     * @param body the body string
     */
    public void setBody(String body) { this.body = body; }

    /**
     * Gets the footer of the commit.
     *
     * @return the footer string
     */
    public String getFooter() { return footer; }

    /**
     * Sets the footer of the commit.
     *
     * @param footer the footer string
     */
    public void setFooter(String footer) { this.footer = footer; }

    /**
     * Builder class for constructing instances of {@link ConventionalCommit}.
     */
    public static class Builder {

        /** Internal instance of the commit being built. */
        private final ConventionalCommit commit = new ConventionalCommit();

        /**
         * Sets the commit type.
         *
         * @param type the {@link BranchType}
         * @return the builder instance
         */
        public Builder type(BranchType type) {
            commit.setType(type);
            return this;
        }

        /**
         * Sets the commit scope.
         *
         * @param scope the scope string
         * @return the builder instance
         */
        public Builder scope(String scope) {
            commit.setScope(scope);
            return this;
        }

        /**
         * Sets the commit description.
         *
         * @param description the summary string
         * @return the builder instance
         */
        public Builder description(String description) {
            commit.setDescription(description);
            return this;
        }

        /**
         * Sets the breaking change indicator.
         *
         * @param isBreaking true if breaking changes are present
         * @return the builder instance
         */
        public Builder isBreaking(boolean isBreaking) {
            commit.setBreaking(isBreaking);
            return this;
        }

        /**
         * Sets the commit body.
         *
         * @param body the body content
         * @return the builder instance
         */
        public Builder body(String body) {
            commit.setBody(body);
            return this;
        }

        /**
         * Sets the commit footer.
         *
         * @param footer the footer content
         * @return the builder instance
         */
        public Builder footer(String footer) {
            commit.setFooter(footer);
            return this;
        }

        /**
         * Builds and returns the completed {@link ConventionalCommit} instance.
         *
         * @return the built ConventionalCommit object
         */
        public ConventionalCommit build() {
            return commit;
        }
    }
}