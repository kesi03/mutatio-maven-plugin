package com.mockholm.mojos;

import com.mockholm.config.Branch;
import com.mockholm.config.BranchType;
import com.mockholm.models.MojoCommons;
import com.mockholm.mojos.commons.BranchMojo;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import static org.apache.maven.plugins.annotations.LifecyclePhase.*;

@Mojo(name = "hotfix-end", aggregator = true, defaultPhase = NONE)
public class HotfixEndMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    @Component
    private Settings settings;

    @Parameter(property = "branch", name = "branch")
    private Branch branch = new Branch();

    public void execute()  {
        new BranchMojo(new MojoCommons()
                .withLog(getLog())
                .withBranch(branch)
                .withProject(project)
                .withSettings(settings))
                .executeEnd(BranchType.HOTFIX);
    }
}
