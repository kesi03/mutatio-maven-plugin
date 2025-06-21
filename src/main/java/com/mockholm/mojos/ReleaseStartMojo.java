package com.mockholm.mojos;

import com.mockholm.config.Branch;
import com.mockholm.config.ReleaseType;
import com.mockholm.config.VersionIdentifier;
import com.mockholm.models.MojoCommons;
import com.mockholm.mojos.commons.ReleaseMojo;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

@Mojo(name = "release-start", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class ReleaseStartMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    @Component
    private Settings settings;

    @Parameter(property = "branch", name = "branch")
    private Branch branch = new Branch();

    @Parameter(name="releaseType", property = "releaseType", defaultValue = "PATCH" , required = true, readonly = false)
    private ReleaseType releaseType;

    @Parameter(name="versionIdentifier", property = "versionIdentifier", defaultValue = "SNAPSHOT" , required = false, readonly = false)
    private VersionIdentifier versionIdentifier;

    public void execute() {
        new ReleaseMojo(new MojoCommons()
                .withLog(getLog())
                .withBranch(branch)
                .withProject(project)
                .withSettings(settings))
                .executeStart(releaseType,versionIdentifier);
    }
}