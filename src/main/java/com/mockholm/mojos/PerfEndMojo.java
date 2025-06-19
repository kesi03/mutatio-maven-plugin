package com.mockholm.mojos;

import com.mockholm.config.Branch;
import com.mockholm.config.BranchType;
import com.mockholm.models.Commons;
import com.mockholm.mojos.commons.BranchMojo;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "perf-end", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class PerfEndMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private String baseDir;

    @Parameter(property = "branch", name = "branch")
    private Branch branch = new Branch();

    public void execute() throws MojoExecutionException, MojoFailureException {
        new BranchMojo(new Commons()
                .withLog(getLog())
                .withBranch(branch)
                .withProject(project)
                .withBaseDir(baseDir))
                .executeEnd(BranchType.PERF);
    }
}
