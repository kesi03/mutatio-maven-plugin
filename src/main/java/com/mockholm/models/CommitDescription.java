package com.mockholm.models;

import com.mockholm.config.BranchAction;

public class CommitDescription {

    private BranchAction action;
    private String branchName;
    private String message;

    private CommitDescription() {}

    public BranchAction getAction() {
        return action;
    }

    public void setAction(BranchAction action) {
        this.action = action;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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

    private static String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static class Builder {
        private final CommitDescription description = new CommitDescription();

        public Builder action(BranchAction action) {
            description.setAction(action);
            return this;
        }

        public Builder branchName(String branchName) {
            description.setBranchName(branchName);
            return this;
        }

        public Builder message(String message) {
            description.setMessage(message);
            return this;
        }

        public CommitDescription build() {
            return description;
        }
    }
}