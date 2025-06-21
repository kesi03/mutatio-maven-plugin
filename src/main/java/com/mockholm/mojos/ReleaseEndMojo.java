package com.mockholm.mojos;

import com.mockholm.config.Branch;
import com.mockholm.config.BranchType;
import com.mockholm.models.MojoCommons;
import com.mockholm.mojos.commons.ReleaseMojo;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

@Mojo(name = "release-end", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class ReleaseEndMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    @Component
    private Settings settings;

    @Parameter(property = "branch", name = "branch")
    private Branch branch = new Branch();

    @Parameter(property = "release", name="release")
    private String release;

    @Parameter(property = "mainOrMaster", name="mainOrMaster", defaultValue = "MASTER")
    private BranchType mainOrMaster;

    public void execute() throws MojoExecutionException, MojoFailureException {

        new ReleaseMojo(new MojoCommons()
                .withLog(getLog())
                .withBranch(branch)
                .withProject(project)
                .withSettings(settings))
                .executeEnd(release,mainOrMaster);
    }
}