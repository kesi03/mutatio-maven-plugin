package com.mockholm.mojos;

import com.mockholm.config.BranchType;
import com.mockholm.models.MojoCommons;
import com.mockholm.mojos.commons.BranchMojoCommons;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import java.util.Optional;

/**
 * This Mojo is used to start the branch.
 * It is typically called at the beginning of the build process to initialize a branch.
 * You must supply a type of branch and repoIndentity
 */
@Mojo(name = "branch-start", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class BranchStartMojo extends AbstractMojo {

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
    @Parameter( defaultValue = "${settings}", readonly = true )
    private Settings settings;

    /**
     * The identity of the repository used to determine the branch to start.
     * This is typically the name of the repository or a unique identifier.
     */
    @Parameter(property = "repoIdentity", name = "repoIdentity")
    private String repoIdentity;

    /**
     * The Branch type {@link BranchType} The type of branch to create (e.g., feat, fix, chore).
     */
    @Parameter(property = "branchType", name = "branchType", defaultValue = "FEATURE")
    private String branchType;



    /**
     * Flag to determine whether to push changes to the remote repository after starting the archive branch.
     * Default is true, meaning changes will be pushed.
     */
    @Parameter(property = "pushChanges", name ="pushChanges", defaultValue = "true")
    private boolean pushChanges;

    /**
     * Optional commit message instead of using a default.
     */
    @Parameter(property = "commitMessage", name ="commitMessage")
    private String commitMessage;

    public void execute() {
        getLog().info(String.format("Creating branch of %s",branchType));
        new BranchMojoCommons(new MojoCommons()
                .withLog(getLog())
                .withRepoIdentity(repoIdentity)
                .withPushChanges(pushChanges)
                .withProject(project)
                .withSettings(settings))
                .executeStart(BranchType.valueOf(branchType),Optional.ofNullable(commitMessage));
    }
}
