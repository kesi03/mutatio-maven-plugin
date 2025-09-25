package com.mockholm.mojos;

import javax.inject.Inject;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.settings.Settings;

import com.mockholm.config.BranchType;
import com.mockholm.config.CollateType;
import com.mockholm.models.MojoCommons;
import com.mockholm.mojos.commons.DependencyMojoCommons;


/**
 * This Mojo is used to report which artifacts are created when a release branch is created
 */
@Mojo(name = "collate-artifacts", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class CollateArtifactsMojo extends AbstractMojo{
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
    @Inject
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
     * The name of the release branch to be created.
     * Default is "release".
     */
    @Parameter(property = "releaseBranch", name ="releaseBranch", defaultValue = "release")
    private String releaseBranch;

    /**
     * The type of collation to be performed.
     * Default is "RELEASE".
     * Options could include "RELEASE", "DEV"
     */
    @Parameter(property = "collateType", name="collateType", defaultValue = "RELEASE")
    private String collateType;

    /**
     * Executes the Mojo to start the dependency collation process.
     * This method initializes the MojoCommons context and calls the DependencyMojoCommons
     * to handle the logic of collating artifacts for the release branch.
     *
     * @throws MojoExecutionException if an error occurs during execution
     * @throws MojoFailureException if a failure occurs that prevents execution
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Starting dependency collation...");
        getLog().info("Release branch: " + releaseBranch);
        getLog().info("Release version: " + release);
        getLog().info("Main or Master branch: " + mainOrMaster);
        getLog().info("------------------------------------");

        // Create a MojoCommons instance to encapsulate the context
        MojoCommons commons = new MojoCommons()
                .withLog(getLog())
                .withProject(currentProject)
                .withSession(session)
                .withSettings(settings)
                .withProjectBuilder(projectBuilder)
                .withReleaseBranch(releaseBranch)
                .withRepoIdentity(repoIdentity);

        // Create an instance of DependencyMojo to handle the logic
        DependencyMojoCommons dependencyMojo = new DependencyMojoCommons(commons);

        try {
            String branchName = dependencyMojo.getReleaseBranchName(releaseBranch);
            getLog().info("Branch: " + branchName);
            // Start the dependency process with the provided parameters
            dependencyMojo.collateArtifacts(branchName, CollateType(collateType));
        } catch (Exception e) {
            throw new MojoExecutionException("Error starting dependency branch", e);
        }
    }

}
