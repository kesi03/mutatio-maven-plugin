package com.mockholm.mojos;

import com.mockholm.commands.PomCommand;
import com.mockholm.config.Branch;
import com.mockholm.config.BranchAction;
import com.mockholm.config.BranchType;
import com.mockholm.models.CommitDescription;
import com.mockholm.models.Commons;
import com.mockholm.models.ConventionalCommit;
import com.mockholm.mojos.commons.BranchMojo;
import com.mockholm.utils.CommitUtils;
import com.mockholm.commands.GitCommand;
import com.mockholm.utils.GitUtils;
import com.mockholm.utils.SemanticVersion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.Optional;

@Mojo(name = "feat-start", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class FeatStartMojo extends AbstractMojo {

        @Parameter(defaultValue = "${project}", required = true, readonly = true)
        private MavenProject project;

        @Parameter(defaultValue = "${project.basedir}", readonly = true)
        private String baseDir;

        @Parameter(property = "branch", name = "branch")
        private Branch branch = new Branch();


        public void execute() {
                new BranchMojo(new Commons().
                        withLog(getLog()).
                        withBranch(branch).
                        withProject(project).
                        withBaseDir(baseDir)).executeStart(BranchType.FEATURE);
        }
}
