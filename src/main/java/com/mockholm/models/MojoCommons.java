package com.mockholm.models;

import com.mockholm.config.Branch;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

public class MojoCommons {
    Log log;
    private MavenProject project;

    private Settings settings;

    private Branch branch;

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public MojoCommons withLog(Log log) {
        this.log = log;
        return this;
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public MojoCommons withProject(MavenProject project) {
        this.project = project;
        return this;
    }

    public Branch getBranch() {
        return branch;
    }

    public MojoCommons withBranch(Branch branch) {
        this.branch = branch;
        return this;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Settings getSettings() {
        return settings;
    }

    public MojoCommons withSettings(Settings settings) {
        this.settings = settings;
        return this;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }
}
