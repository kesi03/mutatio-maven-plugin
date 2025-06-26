package com.mockholm.mojos;

import com.mockholm.config.BranchType;
import com.mockholm.models.MojoCommons;
import com.mockholm.mojos.commons.ReleaseMojo;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

/**
 * This Mojo is used to end the release branch.
 * It is typically called at the end of the build process to finalize the release branch.
 */
@Mojo(name = "release-end", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class ReleaseEndMojo extends AbstractMojo {

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
     * The identity of the repository used to determine the branch to end.
     * This is typically the name of the repository or a unique identifier.
     */
    @Parameter(property = "repoIdentity", name = "repoIdentity")
    private String repoIdentity;

    /**
     * The release version to be used when ending the release branch.
     * This is typically the version number that will be released.
     */
    @Parameter(property = "release", name="release")
    private String release;

    /**
     * The type of branch to be used as the main or master branch after the release.
     * This is typically set to MASTER or MAIN depending on the project's branching strategy.
     */
    @Parameter(property = "mainOrMaster", name="mainOrMaster", defaultValue = "MASTER")
    private BranchType mainOrMaster;

    public void execute() throws MojoExecutionException, MojoFailureException {

        new ReleaseMojo(new MojoCommons()
                .withLog(getLog())
                .withRepoIdentity(repoIdentity)
                .withProject(project)
                .withSettings(settings))
                .executeEnd(release,mainOrMaster);
    }
}