package com.mockholm.mojos;

import java.io.IOException;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.settings.Settings;

import com.mockholm.config.BranchType;
import com.mockholm.models.MojoCommons;
import com.mockholm.mojos.commons.DependencyMojoCommons;

import org.apache.maven.plugins.annotations.LifecyclePhase;

@Mojo(name = "update-dependencies", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
public class UpdateDependenciesMojo extends AbstractMojo {

    /**
     * The Maven project being built.
     * This is used to access project properties and configuration.
     */
    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject currentProject;

    /**
     * The maven session {@link MavenSession}
     */
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    /**
     * Project builder {@link ProjectBuilder}
     */
    @Parameter(defaultValue = "${projectBuilder}", readonly = true)
    private ProjectBuilder projectBuilder;

     /**
     * The settings for the Maven build, which may include repository configurations.
     * This is used to access settings defined in the Maven settings.xml file.
     */
    @Parameter( defaultValue = "${settings}", readonly = true )
    private Settings settings;

    /**
     * The identity of the repository used to determine the branch to start.
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

    /**
     * The artifacts to be updated.
     * This is a comma-separated list of artifact identifiers.
     */
    @Parameter(property = "artifacts", name="artifacts", required = true)
    private String artifacts;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Updating dependencies...");

        // Initialize MojoCommons
       MojoCommons commons = new MojoCommons()
                .withLog(getLog())
                .withProject(currentProject)
                .withSession(session)
                .withSettings(settings)
                .withProjectBuilder(projectBuilder)
                .withRepoIdentity(repoIdentity);

        // Create an instance of DependencyMojo to handle dependency updates
        DependencyMojoCommons dependencyMojo = new DependencyMojoCommons(commons);
        
        try {
            // Call the method to update dependencies
           dependencyMojo.updateDependencies(release, mainOrMaster, artifacts);

        } catch (IOException e) {
            throw new MojoExecutionException("Failed to update dependencies", e);
        }
    }

}
