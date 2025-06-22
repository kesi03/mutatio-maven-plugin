package com.mockholm.config;

import org.apache.maven.plugins.annotations.Parameter;

public class Credential {
    @Parameter(property = "credential.id")
    private String id;
    @Parameter(property = "credential.userName")
    private String username;
    @Parameter(property = "credential.password")
    private String password;
    @Parameter(property = "credential.privateKey")
    private String privateKey;
    @Parameter(property = "credential.passphrase")
    private String passphrase;
    @Parameter(defaultValue = "READ", property ="credential.action")
    private SettingsAction action;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public SettingsAction getAction() {
        return action;
    }

    public void setAction(SettingsAction action) {
        this.action = action;
    }
}
