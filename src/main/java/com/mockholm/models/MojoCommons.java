package com.mockholm.models;

import com.mockholm.config.Branch;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.settings.Settings;

/**
 * Encapsulates common components shared across Maven plugin Mojos,
 * including project configuration, versioning branch details, credentials,
 * logging interface, and push behavior settings.
 */
public class MojoCommons {

    /** Logger provided by the Maven plugin framework. */
    Log log;

    /** Maven project model representing the current module's metadata and structure. */
    private MavenProject project;

    /** Maven user settings including credentials, repositories, and proxies. */
    private Settings settings;

    /** Current Maven session, providing context for the build lifecycle. */
    private MavenSession session;

    /** Project builder for constructing Maven project models from POM files. */
    private ProjectBuilder projectBuilder;

    /** The branch configuration, often used for deriving versioning strategy. */
    private Branch branch;

    /** Identity string representing the repository, e.g., "group/artifact". */
    private String repoIdentity;

    /** Flag indicating whether SCM changes should be pushed automatically. */
    private boolean pushChanges;

    /**
     * Gets the Maven logger.
     *
     * @return the Log instance
     */
    public Log getLog() {
        return log;
    }

    /**
     * Sets the Maven logger.
     *
     * @param log the Log instance
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Sets the Maven logger and returns the updated instance.
     *
     * @param log the Log instance
     * @return the updated MojoCommons object
     */
    public MojoCommons withLog(Log log) {
        this.log = log;
        return this;
    }

    /**
     * Gets the Maven project.
     *
     * @return the MavenProject object
     */
    public MavenProject getProject() {
        return project;
    }

    /**
     * Sets the Maven project.
     *
     * @param project the MavenProject object
     */
    public void setProject(MavenProject project) {
        this.project = project;
    }

    /**
     * Sets the Maven project and returns the updated instance.
     *
     * @param project the MavenProject object
     * @return the updated MojoCommons object
     */
    public MojoCommons withProject(MavenProject project) {
        this.project = project;
        return this;
    }

    /**
     * Gets the configured branch object.
     *
     * @return the Branch object
     */
    public Branch getBranch() {
        return branch;
    }

    /**
     * Sets the branch object and returns the updated instance.
     *
     * @param branch the Branch object
     * @return the updated MojoCommons object
     */
    public MojoCommons withBranch(Branch branch) {
        this.branch = branch;
        return this;
    }

    /**
     * Sets the branch object.
     *
     * @param branch the Branch object
     */
    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    /**
     * Gets the Maven settings.
     *
     * @return the Settings object
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Sets the Maven settings and returns the updated instance.
     *
     * @param settings the Settings object
     * @return the updated MojoCommons object
     */
    public MojoCommons withSettings(Settings settings) {
        this.settings = settings;
        return this;
    }

    /**
     * Sets the Maven settings.
     *
     * @param settings the Settings object
     */
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * Gets the repository identity string.
     *
     * @return the repo identity string
     */
    public String getRepoIdentity() {
        return repoIdentity;
    }

    /**
     * Sets the repository identity and returns the updated instance.
     *
     * @param repoIdentity the repo identity string
     * @return the updated MojoCommons object
     */
    public MojoCommons withRepoIdentity(String repoIdentity) {
        this.repoIdentity = repoIdentity;
        return this;
    }

    /**
     * Sets the repository identity string.
     *
     * @param repoIdentity the repo identity string
     */
    public void setRepoIdentity(String repoIdentity) {
        this.repoIdentity = repoIdentity;
    }

    /**
     * Indicates whether SCM changes should be pushed.
     *
     * @return true if pushing is enabled, false otherwise
     */
    public boolean isPushChanges() {
        return pushChanges;
    }

    /**
     * Sets the push behavior and returns the updated instance.
     *
     * @param pushChanges true to enable pushing changes
     * @return the updated MojoCommons object
     */
    public MojoCommons withPushChanges(boolean pushChanges) {
        this.pushChanges = pushChanges;
        return this;
    }

    /**
     * Sets the push behavior.
     *
     * @param pushChanges true to enable pushing changes
     */
    public void setPushChanges(boolean pushChanges) {
        this.pushChanges = pushChanges;
    }

    /**
     * Sets the Maven session and returns the updated instance.
     *
     * @param session the MavenSession object
     * @return the updated MojoCommons object
     */
    public MojoCommons withSession(MavenSession session) {
        this.session = session;
        return this;    
    }

    /**
     * Sets the Maven session.
     *
     * @param session the MavenSession object
     */
    public void setSession(MavenSession session) {
        this.session = session;
    }

    /**
     * Gets the current Maven session.
     *
     * @return the MavenSession object
     */
    public MavenSession getSession() {
        return session;
    }

    /**
     * Sets the project builder and returns the updated instance.
     *
     * @param projectBuilder the ProjectBuilder object
     * @return the updated MojoCommons object
     */
    public MojoCommons withProjectBuilder(ProjectBuilder projectBuilder) {
        this.projectBuilder = projectBuilder;
        return this;
    }

    /**
     * Sets the project builder.
     *
     * @param projectBuilder the ProjectBuilder object
     */
    public void setProjectBuilder(ProjectBuilder projectBuilder) {
        this.projectBuilder = projectBuilder;
    }

    /**
     * Gets the project builder.
     *
     * @return the ProjectBuilder object
     */
    public ProjectBuilder getProjectBuilder() {
        return projectBuilder;
    }
    
}