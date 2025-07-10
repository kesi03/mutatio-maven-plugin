package com.mockholm.mojos;

import com.mockholm.config.SettingsAction;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.DefaultSettingsReader;
import org.apache.maven.settings.io.DefaultSettingsWriter;
import org.codehaus.plexus.util.FileUtils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * This Mojo is used to update the Maven settings.xml file with server credentials.
 * It can add, update, or remove server entries based on the provided parameters.
 */     
@Mojo(name = "update-settings", defaultPhase = LifecyclePhase.VALIDATE)
public class UpdateSettingsMojo extends AbstractMojo {
        /**
         * The Maven project being built.
         * This is used to access project properties and configuration.
         */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

        /**
         * The settings for the Maven build, which may include repository configurations.
         * This is used to access settings defined in the Maven settings.xml file.
         */
    @Parameter(defaultValue = "${settings}", readonly = true, required = true)
    private Settings settings;

        /**
         * The ID of the server to be added, updated, or removed.
         * This is typically the name of the server as defined in the settings.xml file.
         */
    @Parameter(property = "id")
    private String id;

        /**
         * The password for the server.
         * This is used to authenticate with the server.
         */
    @Parameter(property = "password")
    private String password;

        /**
         * The privateKey.
         * This is used to authenticate with the server.
         */
    @Parameter(property = "privateKey")
    private String privateKey;

        /**
         * The ssh key passphrase.
         * This is used to authenticate with the server.
         */
    @Parameter(property = "passphrase")
    private String passphrase;

        /**
         * The username for the server.
         * This is used to authenticate with the server.
         */
    @Parameter(property = "userName")
    private String userName;

        /**
         * The action to be performed on the server.
         * This determines whether to read, add, update, or remove the server entry.
         */
    @Parameter(property = "action", defaultValue = "READ")
    private SettingsAction action;

        /**
         * Executes the Mojo to update the Maven settings.xml file.
         * It reads the settings, performs the specified action (READ, ADD, UPDATE, REMOVE),
         * and writes the changes back to the settings.xml file.
         */
    public void execute() {
        if (project.isExecutionRoot()) {
            Path settingsPath = Paths.get(System.getProperty("user.home"), ".m2", "settings.xml");
            String mavenSettings = String.valueOf(settingsPath.toAbsolutePath().normalize());
            if (FileUtils.fileExists(String.valueOf(settingsPath.toAbsolutePath().normalize()))) {
                getLog().info("settings: " + mavenSettings);
            }
            try {
                Settings settings = new DefaultSettingsReader().read(new File(mavenSettings), null);
                getLog().info("Action: " + action);
                Optional<String> idValue = Optional.ofNullable(id)
                        .filter(s -> !s.isEmpty());
                if (idValue.isPresent()) {
                    Server server = (action == SettingsAction.READ) ? settings.getServer(idValue.get()) : new Server();
                    server.setId(id);
                    Optional<String> userNameValue = Optional.ofNullable(userName)
                            .filter(s -> !s.isEmpty());

                    Optional<String> passwordValue = Optional.ofNullable(password)
                            .filter(s -> !s.isEmpty());

                    Optional<String> passPhraseValue = Optional.ofNullable(passphrase)
                            .filter(s -> !s.isEmpty());

                    Optional<String> privateKeyValue = Optional.ofNullable(privateKey)
                            .filter(s -> !s.isEmpty());
                    getLog().info("Server id: " + idValue.get());
                    switch (action) {
                        case READ:
                            userNameValue.ifPresent(value -> getLog().info("userName: " + value));
                            passwordValue.ifPresent(value -> getLog().info("password: " + value));
                            passPhraseValue.ifPresent(value -> getLog().info("passPhrase: " + value));
                            privateKeyValue.ifPresent(value -> getLog().info("privateKey: " + value));
                            break;
                        case ADD:
                        case UPDATE:
                            // Add or update
                            userNameValue.ifPresent(server::setUsername);
                            passwordValue.ifPresent(server::setPassword);
                            passPhraseValue.ifPresent(server::setPassphrase);
                            privateKeyValue.ifPresent(value -> server.setPrivateKey("${user.home}" + privateKeyValue.get()));
                            settings.addServer(server);
                            new DefaultSettingsWriter().write(new File(mavenSettings), null, settings);
                            break;
                        case REMOVE:
                            // Remove
                            settings.getServers().removeIf(s -> s.getId().equals(idValue.get()));
                            new DefaultSettingsWriter().write(new File(mavenSettings), null, settings);
                            break;
                    }
                } else {
                    settings.getServers().forEach(server -> {
                        Optional.ofNullable(server.getId())
                                .filter(s -> !s.isBlank())
                                .ifPresent(id -> getLog().info("Server ID: " + id));

                        Optional.ofNullable(server.getUsername())
                                .filter(s -> !s.isBlank())
                                .ifPresent(username -> getLog().info("Username: " + username));

                        Optional.ofNullable(server.getPassword())
                                .filter(s -> !s.isBlank())
                                .ifPresent(password -> getLog().info("Password: " + password));

                        Optional.ofNullable(server.getPrivateKey())
                                .filter(s -> !s.isBlank())
                                .ifPresent(key -> getLog().info("Private Key: " + key));

                        Optional.ofNullable(server.getPassphrase())
                                .filter(s -> !s.isBlank())
                                .ifPresent(passphrase -> getLog().info("Passphrase: " + passphrase));

                        Optional.ofNullable(server.getFilePermissions())
                                .filter(s -> !s.isBlank())
                                .ifPresent(fp -> getLog().info("File Permissions: " + fp));

                        Optional.ofNullable(server.getDirectoryPermissions())
                                .filter(s -> !s.isBlank())
                                .ifPresent(dp -> getLog().info("Directory Permissions: " + dp));

                        Optional.ofNullable(server.getConfiguration())
                                .ifPresent(config -> getLog().info("Configuration: " + config));

                        getLog().info("-------------------------------------");
                    });
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
