package com.mockholm.config;

public enum BranchAction {
    START("start"),
    FINISH("finish"),
    MERGE("merge"),
    UPDATE("update");

    private final String value;

    BranchAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}