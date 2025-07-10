package com.mockholm.config;

import org.apache.maven.model.Scm;
import org.apache.maven.settings.Settings;

/**
 * Encapsulates Git-related configuration including authentication,
 * source control metadata, Maven settings, and push behavior.
 */
public class GitConfiguration {

    /** The key used to authenticate with the Git server. */
    String serverKey;

    /** Source Control Management (SCM) metadata extracted from Maven's project model. */
    Scm scm;

    /** Maven settings, usually from settings.xml, including credentials and proxies. */
    Settings settings;

    /** Indicates whether changes should be pushed to the remote Git repository. */
    boolean pushChanges;

    /**
     * Gets the Git server authentication key.
     *
     * @return the server key
     */
    public String getServerKey() {
        return serverKey;
    }

    /**
     * Sets the Git server key and returns the current instance.
     * Useful for method chaining.
     *
     * @param serverKey the server key
     * @return the updated GitConfiguration object
     */
    public GitConfiguration withServerKey(String serverKey) {
        this.serverKey = serverKey;
        return this;
    }

    /**
     * Sets the Git server key and returns the current instance.
     *
     * @param serverKey the server key
     * @return the updated GitConfiguration object
     */
    public GitConfiguration setServerKey(String serverKey) {
        this.serverKey = serverKey;
        return this;
    }

    /**
     * Gets the SCM configuration.
     *
     * @return the Scm object
     */
    public Scm getScm() {
        return scm;
    }

    /**
     * Sets the SCM configuration and returns the current instance.
     *
     * @param scm the Scm object
     * @return the updated GitConfiguration object
     */
    public GitConfiguration withScm(Scm scm) {
        this.scm = scm;
        return this;
    }

    /**
     * Sets the SCM configuration.
     *
     * @param scm the Scm object
     */
    public void setScm(Scm scm) {
        this.scm = scm;
    }

    /**
     * Gets the Maven user settings.
     *
     * @return the Settings object
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Sets the Maven user settings.
     *
     * @param settings the Settings object
     */
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * Sets the Maven user settings and returns the current instance.
     *
     * @param settings the Settings object
     * @return the updated GitConfiguration object
     */
    public GitConfiguration withSettings(Settings settings) {
        this.settings = settings;
        return this;
    }

    /**
     * Indicates whether Git changes should be pushed to the remote repository.
     *
     * @return true if changes will be pushed, false otherwise
     */
    public boolean isPushChanges() {
        return pushChanges;
    }

    /**
     * Sets the pushChanges flag and returns the current instance.
     *
     * @param pushChanges true to enable pushing changes
     * @return the updated GitConfiguration object
     */
    public GitConfiguration withPushChanges(boolean pushChanges) {
        this.pushChanges = pushChanges;
        return this;
    }

    /**
     * Sets the pushChanges flag.
     *
     * @param pushChanges true to enable pushing changes
     */
    public void setPushChanges(boolean pushChanges) {
        this.pushChanges = pushChanges;
    }
}