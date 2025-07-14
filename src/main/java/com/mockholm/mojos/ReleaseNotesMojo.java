package com.mockholm.mojos;

import com.mockholm.commands.GitCommand;
import com.mockholm.config.*;
import com.mockholm.models.MojoCommons;
import com.mockholm.mojos.commons.ReleaseMojo;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import java.io.IOException;

/**
 * This Mojo is used to start the release branch.
 * It is typically called at the beginning of the build process to initialize the release branch.
 */
@Mojo(name = "release-notes", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class ReleaseNotesMojo extends AbstractMojo {

    /**
     * The Maven project being built.
     * This is used to access project properties and configuration.
     */
    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject project;

    /**
     * The settings for the Maven build, which may include repository configurations.
     * This is used to access settings defined in the Maven settings.xml file.
     */
    @Parameter( defaultValue = "${settings}", readonly = true)
    private Settings settings;

    /**
     * Used to choose which kind of notes you want.
     * {@link ReleaseNotesType}
     */
    @Parameter(property = "notesType", name = "notesType")
    private ReleaseNotesType notesType;

    /**
     * Used to determine which release tag you wish to create notes for
     */
    @Parameter(property = "release", name ="release")
    private String releaseTag;

    public void execute() {
        GitConfiguration gitConfiguration = new GitConfiguration()
                .withServerKey(project.getProperties().getProperty("gitProvider"))
                .withScm(project.getScm())
                .withSettings(settings);
        try {
            switch(notesType){
                case BRANCH:
                    new GitCommand(getLog())
                            .generateBranchTypeReleaseNotes(releaseTag,gitConfiguration,".")
                            .close();
                    break;
                case CATEGORY:
                    new GitCommand(getLog()).generateCategorizedReleaseNotes(releaseTag,gitConfiguration,".")
                            .close();
                case STANDARD:
                default:
                    new GitCommand(getLog())
                            .generateReleaseNotes(releaseTag,gitConfiguration,".")
                            .close();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}