package com.mockholm.models;

import com.mockholm.config.BranchType;

public class ConventionalCommit {

    private BranchType type;
    private String scope;
    private String description;
    private boolean isBreaking;
    private String body;
    private String footer;

    private ConventionalCommit() {}

    public BranchType getType() { return type; }
    public void setType(BranchType type) { this.type = type; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isBreaking() { return isBreaking; }
    public void setBreaking(boolean breaking) { isBreaking = breaking; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getFooter() { return footer; }
    public void setFooter(String footer) { this.footer = footer; }

    public static class Builder {
        private final ConventionalCommit commit = new ConventionalCommit();

        public Builder type(BranchType type) {
            commit.setType(type);
            return this;
        }

        public Builder scope(String scope) {
            commit.setScope(scope);
            return this;
        }

        public Builder description(String description) {
            commit.setDescription(description);
            return this;
        }

        public Builder isBreaking(boolean isBreaking) {
            commit.setBreaking(isBreaking);
            return this;
        }

        public Builder body(String body) {
            commit.setBody(body);
            return this;
        }

        public Builder footer(String footer) {
            commit.setFooter(footer);
            return this;
        }

        public ConventionalCommit build() {
            return commit;
        }
    }
}