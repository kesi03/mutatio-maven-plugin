package com.mockholm.config;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Represents credentials used for accessing secure systems or services,
 * including username/password authentication, SSH key-based authentication,
 * and optional action flags.
 */
public class Credential {

    /** Unique identifier for the credential, typically used for reference or lookup. */
    @Parameter(property = "credential.id")
    private String id;

    /** Username associated with the credential, used for authentication. */
    @Parameter(property = "credential.userName")
    private String username;

    /** Password associated with the credential, used for authentication. */
    @Parameter(property = "credential.password")
    private String password;

    /** Private key used for SSH-based authentication. */
    @Parameter(property = "credential.privateKey")
    private String privateKey;

    /** Optional passphrase to unlock the associated private key. */
    @Parameter(property = "credential.passphrase")
    private String passphrase;

    /** Action associated with the credential, defaulting to READ. */
    @Parameter(defaultValue = "READ", property ="credential.action")
    private SettingsAction action;

    /**
     * Gets the credential ID.
     *
     * @return the credential ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the credential ID.
     *
     * @param id the credential ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the private key.
     *
     * @return the private key string
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Sets the private key.
     *
     * @param privateKey the private key string
     */
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Gets the password.
     *
     * @return the password string
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the password string
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the passphrase used for unlocking the private key.
     *
     * @return the passphrase string
     */
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Sets the passphrase used for unlocking the private key.
     *
     * @param passphrase the passphrase string
     */
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    /**
     * Gets the action associated with the credential.
     *
     * @return the {@link SettingsAction}
     */
    public SettingsAction getAction() {
        return action;
    }

    /**
     * Sets the action associated with the credential.
     *
     * @param action the {@link SettingsAction}
     */
    public void setAction(SettingsAction action) {
        this.action = action;
    }
}