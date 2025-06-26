package com.mockholm.mojos;

import com.mockholm.config.BranchType;
import com.mockholm.models.MojoCommons;
import com.mockholm.mojos.commons.BranchMojo;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

/**
 * This Mojo is used to end the bugfix branch.
 * It is typically called at the end of the build process to finalize the bugfix branch.
 */
@Mojo(name = "bugfix-end", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class BugfixEndMojo extends AbstractMojo {

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
     * The identity of the repository used to determine the branch to end.
     * This is typically the name of the repository or a unique identifier.
     */
    @Parameter(property = "repoIdentity", name = "repoIdentity")
    private String repoIdentity;

    @Parameter(property = "pushChanges", name ="pushChanges", defaultValue = "true")
    private boolean pushChanges;


    public void execute()  {
        new BranchMojo(new MojoCommons()
                .withLog(getLog())
                .withRepoIdentity(repoIdentity)
                .withPushChanges(pushChanges)
                .withProject(project)
                .withSettings(settings))
                .executeEnd(BranchType.BUGFIX);
    }
}
