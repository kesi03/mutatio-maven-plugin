package com.mockholm.commands;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Command utility for reading, updating, and propagating version information
 * across a Maven {@code pom.xml} file and its modules.
 * <p>
 * Designed for use in plugin contexts where logging and lifecycle control are needed.
 */
public class PomCommand {
    private final Model model;
    private String version;
    private final Log log;
    private final String baseDir;

    /**
     * Constructs a {@code PomCommand} for the specified base directory,
     * loading the primary {@code pom.xml} into memory.
     *
     * @param baseDir the root directory containing the {@code pom.xml}
     * @param log the Maven plugin logger for output messages
     * @throws RuntimeException if the {@code pom.xml} cannot be read
     */
    public PomCommand(String baseDir, Log log) {
        File pomFile = new File(baseDir, "pom.xml");

        try (FileReader reader = new FileReader(pomFile)) {
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            this.model = pomReader.read(reader);
            this.log = log;
            this.baseDir = baseDir;
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the version to be used for future updates.
     *
     * @param version the version string to apply (e.g. {@code "1.2.3"})
     * @return this {@code PomCommand} instance for chaining
     */
    public PomCommand setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Updates the main {@code pom.xml} file with the version previously set via {@link #setVersion(String)}.
     *
     * @return this {@code PomCommand} instance
     * @throws MojoExecutionException if the POM file cannot be read or written
     */
    public PomCommand updatePomVersion() throws MojoExecutionException {
        File pomFile = new File(baseDir, "pom.xml");

        try (FileReader reader = new FileReader(pomFile)) {
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            Model model = pomReader.read(reader);

            model.setVersion(version);

            try (FileWriter writer = new FileWriter(pomFile)) {
                MavenXpp3Writer pomWriter = new MavenXpp3Writer();
                pomWriter.write(writer, model);
            }

            log.info("Version updated to " + version);

        } catch (IOException | XmlPullParserException e) {
            throw new MojoExecutionException("Failed to update pom.xml", e);
        }

        return this;
    }

    /**
     * Iterates through all declared modules in the parent {@code pom.xml} and updates
     * their parent version references to match the current version.
     *
     * @return this {@code PomCommand} instance
     * @throws MojoExecutionException if any module fails to update
     */
    public PomCommand updateModules() throws MojoExecutionException {
        if (model.getModules() != null && !model.getModules().isEmpty()) {
            for (String module : model.getModules()) {
                File modulePom = new File(module, "pom.xml");
                if (modulePom.exists()) {
                    String moduleBaseDir = modulePom.getParentFile().getAbsolutePath();
                    log.info("Updating module: " + moduleBaseDir);
                    updateModuleParentVersion(moduleBaseDir);
                } else {
                    log.warn("Module pom.xml not found: " + modulePom.getAbsolutePath());
                }
            }
        }
        return this;
    }

    /**
     * Updates the parent version in a module's {@code pom.xml}.
     * Called internally from {@link #updateModules()}.
     *
     * @param baseDir the directory where the module's {@code pom.xml} resides
     * @throws MojoExecutionException if the module POM cannot be updated
     */
    private void updateModuleParentVersion(String baseDir) throws MojoExecutionException {
        File pomFile = new File(baseDir, "pom.xml");

        try (FileReader reader = new FileReader(pomFile)) {
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            Model model = pomReader.read(reader);

            if (model.getParent() != null) {
                model.getParent().setVersion(version);
            } else {
                log.warn("No parent defined in pom.xml");
            }

            try (FileWriter writer = new FileWriter(pomFile)) {
                MavenXpp3Writer pomWriter = new MavenXpp3Writer();
                pomWriter.write(writer, model);
            }

            log.info("Parent version updated to " + version);

        } catch (IOException | XmlPullParserException e) {
            throw new MojoExecutionException("Failed to update parent version in pom.xml", e);
        }
    }

    

}
