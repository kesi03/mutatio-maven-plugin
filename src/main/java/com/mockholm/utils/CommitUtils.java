package com.mockholm.utils;


import com.mockholm.config.BranchType;
import com.mockholm.models.ConventionalCommit;

/**
 * Utility class for working with Conventional Commits.
 * Provides methods for formatting a {@link ConventionalCommit} into a standardized message,
 * and for parsing a commit message string back into a {@code ConventionalCommit} object.
 */
public class CommitUtils {

    /**
     * Formats a {@link ConventionalCommit} instance into a conventional commit message string.
     * Follows the format: {@code type(scope)!: description} with optional body and footer.
     *
     * @param commit the ConventionalCommit object to format
     * @return a formatted commit message string
     */
    public static String format(ConventionalCommit commit) {
        StringBuilder sb = new StringBuilder();

        sb.append(commit.getType().getValue());
        if (commit.getScope() != null && !commit.getScope().isBlank()) {
            sb.append("(").append(commit.getScope()).append(")");
        }
        if (commit.isBreaking()) {
            sb.append("!");
        }
        sb.append(": ").append(commit.getDescription());

        if (commit.getBody() != null && !commit.getBody().isBlank()) {
            sb.append("\n\n").append(commit.getBody().trim());
        }

        if (commit.getFooter() != null && !commit.getFooter().isBlank()) {
            sb.append("\n\n").append(commit.getFooter().trim());
        }

        return sb.toString();
    }

    /**
     * Parses a conventional commit message string into a {@link ConventionalCommit} object.
     * Expects the format: {@code type(scope)!: description}, optionally followed by body and footer,
     * separated by double line breaks.
     *
     * @param message the commit message string to parse
     * @return a ConventionalCommit object representing the parsed message
     * @throws IllegalArgumentException if the commit message does not follow the expected format
     *                                  or the commit type is not recognized
     */
    public static ConventionalCommit parse(String message) {
        String[] parts = message.split("\\R\\R", 3);
        String header = parts[0];
        String body = parts.length > 1 ? parts[1] : null;
        String footer = parts.length > 2 ? parts[2] : null;

        String typeStr = null, scope = null, description;
        boolean isBreaking = false;

        int colonIndex = header.indexOf(":");
        if (colonIndex == -1) throw new IllegalArgumentException("Invalid commit message format");

        String prefix = header.substring(0, colonIndex).trim();
        description = header.substring(colonIndex + 1).trim();

        if (prefix.contains("!")) isBreaking = true;

        int scopeStart = prefix.indexOf('(');
        int scopeEnd = prefix.indexOf(')');

        if (scopeStart != -1 && scopeEnd != -1 && scopeEnd > scopeStart) {
            typeStr = prefix.substring(0, scopeStart);
            scope = prefix.substring(scopeStart + 1, scopeEnd);
        } else {
            typeStr = prefix.replace("!", "");
        }

        BranchType type = null;
        for (BranchType bt : BranchType.values()) {
            if (bt.getValue().equals(typeStr)) {
                type = bt;
                break;
            }
        }

        if (type == null) {
            throw new IllegalArgumentException("Unknown commit type: " + typeStr);
        }

        return new ConventionalCommit.Builder()
                .type(type)
                .scope(scope)
                .description(description)
                .isBreaking(isBreaking)
                .body(body)
                .footer(footer)
                .build();
    }
}
