package com.mockholm.mojos;

import com.mockholm.config.BranchType;
import com.mockholm.models.MojoCommons;
import com.mockholm.mojos.commons.BranchMojo;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

/**
 * This Mojo is used to start a feature branch.
 * It is typically called at the beginning of the development process to initialize a new feature branch.
 */
@Mojo(name = "feat-start", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class FeatStartMojo extends AbstractMojo {

    /**
     * The Maven project being built.
     * This is used to access project properties and configuration.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * The settings for the Maven build, which may include repository configurations.
     * This is used to access settings defined in the Maven settings.xml file.
     */
    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    /**
     * The identity of the repository used to determine the branch to start.
     * This is typically the name of the repository or a unique identifier.
     */
    @Parameter(property = "repoIdentity", name = "repoIdentity")
    private String repoIdentity;

    /**
     * The plugin descriptor, which provides metadata about the plugin.
     * This is used to access the plugin version and other details.
     */
    @Parameter(defaultValue = "${plugin}", readonly = true)
    private PluginDescriptor pluginDescriptor;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;


    /**
     * Flag to determine whether to push changes to the remote repository after starting the archive branch.
     * Default is true, meaning changes will be pushed.
     */
    @Parameter(property = "pushChanges", name = "pushChanges", defaultValue = "true")
    private boolean pushChanges;

    public void execute() {
        String version = pluginDescriptor.getVersion();
        getLog().info("Running plugin version: " + version);
        getLog().info("Active profiles: " + session.getRequest().getActiveProfiles());
        getLog().info("repoIndentity:"+repoIdentity);
        new BranchMojo(new MojoCommons().
                withLog(getLog()).
                withPushChanges(pushChanges).
                withRepoIdentity(repoIdentity).
                withProject(project).
                withSettings(settings)).executeStart(BranchType.FEATURE);
    }
}
