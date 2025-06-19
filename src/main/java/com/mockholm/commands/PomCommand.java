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

public class PomCommand {
    private final Model model;
    private String version;
    private final Log log;
    private final String baseDir;

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

    public PomCommand setVersion(String version) {
        this.version = version;
        return this;
    }

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
