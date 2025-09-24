package com.mockholm.mojos;

import com.mockholm.config.ReleaseType;
import com.mockholm.config.VersionIdentifier;
import com.mockholm.models.MojoCommons;
import com.mockholm.mojos.commons.ReleaseMojoCommons;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

/**
 * This Mojo is used to start the release branch.
 * It is typically called at the beginning of the build process to initialize the release branch.
 */
@Mojo(name = "release-start", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class ReleaseStartMojo extends AbstractMojo {

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
     * The identity of the repository used to determine the branch to start.
     * This is typically the name of the repository or a unique identifier.
     */
    @Parameter(property = "repoIdentity", name = "repoIdentity")
    private String repoIdentity;

    /**
     * The type of release to be started.
     * This is typically set to PATCH, MINOR, or MAJOR depending on the project's release strategy.
     */
    @Parameter(name="releaseType", property = "releaseType", defaultValue = "PATCH" , required = true, readonly = false)
    private ReleaseType releaseType;

    /**
     * The version identifier to be used for the release.
     * This is typically set to SNAPSHOT or a specific version number.
     */
    @Parameter(name="versionIdentifier", property = "versionIdentifier", defaultValue = "SNAPSHOT" , required = false, readonly = false)
    private VersionIdentifier versionIdentifier;

     /**
     * Flag to determine whether to push changes to the remote repository after starting the archive branch.
     * Default is true, meaning changes will be pushed.
     */
    @Parameter(property = "pushChanges", name ="pushChanges", defaultValue = "true")
    private boolean pushChanges;

    /**
     * The name of the branch to be released.
     * Default is "develop".
     */
    @Parameter(property = "devBranch", name ="devBranch", defaultValue = "develop")
    private String devBranch;

    /**
     * The name of the release branch to be created.
     * Default is "release".
     */
    @Parameter(property = "releaseBranch", name ="releaseBranch", defaultValue = "release")
    private String releaseBranch;

    public void execute() {
        new ReleaseMojoCommons(new MojoCommons()
                .withLog(getLog())
                .withPushChanges(pushChanges)
                .withRepoIdentity(repoIdentity)
                .withDevBranch(devBranch)
                .withReleaseBranch(releaseBranch)
                .withProject(project)
                .withSettings(settings))
                .executeStart(releaseType,versionIdentifier);
    }
}