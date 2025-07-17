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

/**
 * This Mojo is used to end a branch.
 * It is typically called at the end of the build process to finalize the branch.
 * You must supply a type of branch and repoIndentity
 */
@Mojo(name = "branch-end", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class BranchEndMojo extends AbstractMojo {

    /**
     * The Maven project being built.
     * This is used to access project properties and configuration.
     */
    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject project;

    /**
     * The identity of the repository used to determine the branch to end.
     * This is typically the name of the repository or a unique identifier.
     */
    @Parameter(property = "repoIdentity", name = "repoIdentity")
    private String repoIdentity;

    /**
     * A flag indicating whether to push changes to the remote repository after ending the branch.
     * If set to true, the changes will be pushed; otherwise, they will not.
     */
    @Parameter(property = "pushChanges", name ="pushChanges", defaultValue = "true")
    private boolean pushChanges;

    /**
     * The settings for the Maven build, which may include repository configurations.
     * This is used to access settings defined in the Maven settings.xml file.
     */
    @Parameter( defaultValue = "${settings}", readonly = true )
    private Settings settings;

    /**
     * The Branch type {@link BranchType}
     */
    @Parameter(property = "branchType", name = "branchType", defaultValue = "FEATURE")
    private String branchType;

    public void execute() {
        getLog().info(String.format("Creating branch of %s",branchType));
        new BranchMojoCommons(new MojoCommons()
                .withLog(getLog())
                .withProject(project)
                .withSettings(settings)
                .withPushChanges(pushChanges)
                .withRepoIdentity(repoIdentity)
        ).executeEnd(BranchType.valueOf(branchType));
    }
}
