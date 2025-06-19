package com.mockholm.config;

import org.apache.maven.plugins.annotations.Parameter;

public class Branch {

    @Parameter
    private String name;

    @Parameter
    private String description;

    @Parameter
    private BranchType branchType;

    public String getName() {
        return name;
    }

    public Branch withName(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public Branch withDescription(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BranchType getBranchType() {
        return branchType;
    }

    public Branch withBranchType(BranchType branchType) {
        this.branchType = branchType;
        return this;
    }

    public void setBranchType(BranchType branchType) {
        this.branchType = branchType;
    }
}
