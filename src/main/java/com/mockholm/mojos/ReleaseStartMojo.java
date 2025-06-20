package com.mockholm.mojos;

import com.mockholm.config.Branch;
import com.mockholm.config.ReleaseType;
import com.mockholm.config.VersionIdentifier;
import com.mockholm.models.MojoCommons;
import com.mockholm.mojos.commons.ReleaseMojo;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "release-start", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class ReleaseStartMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private String baseDir;

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
                .withBaseDir(baseDir))
                .executeStart(releaseType,versionIdentifier);
    }
}