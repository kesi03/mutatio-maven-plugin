package com.mockholm.models;

import com.mockholm.config.Branch;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class Commons {
    Log log;
    private MavenProject project;

    private String baseDir;


    private Branch branch;

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Commons withLog(Log log) {
        this.log = log;
        return this;
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public Commons withProject(MavenProject project) {
        this.project = project;
        return this;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public Commons withBaseDir(String baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public Branch getBranch() {
        return branch;
    }

    public Commons withBranch(Branch branch) {
        this.branch = branch;
        return this;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }
}
