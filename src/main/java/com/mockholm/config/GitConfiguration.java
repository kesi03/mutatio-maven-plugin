package com.mockholm.config;

import org.apache.maven.model.Scm;
import org.apache.maven.settings.Settings;

public class GitConfiguration {
    String serverKey;
    Scm scm;
    Settings settings;
    boolean pushChanges;

    public String getServerKey() {
        return serverKey;
    }

    public GitConfiguration withServerKey(String serverKey) {
        this.serverKey = serverKey;
        return this;
    }

    public GitConfiguration setServerKey(String serverKey) {
        this.serverKey = serverKey;
        return this;
    }

    public Scm getScm() {
        return scm;
    }

    public GitConfiguration withScm(Scm scm) {
        this.scm = scm;
        return this;
    }

    public void setScm(Scm scm) {
        this.scm = scm;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public GitConfiguration withSettings(Settings settings) {
        this.settings = settings;
        return this;
    }

    public boolean isPushChanges() {
        return pushChanges;
    }

    public GitConfiguration withPushChanges(boolean pushChanges) {
        this.pushChanges = pushChanges;
        return this;
    }

    public void setPushChanges(boolean pushChanges) {
        this.pushChanges = pushChanges;
    }
}
