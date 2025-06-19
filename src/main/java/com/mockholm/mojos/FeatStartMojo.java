package com.mockholm.mojos;

import com.mockholm.config.Branch;
import com.mockholm.config.BranchType;
import com.mockholm.utils.GitUtils;
import com.mockholm.utils.SemanticVersion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.Optional;

@Mojo(name = "feat-start",aggregator = true, defaultPhase = LifecyclePhase.INITIALIZE)
public class FeatStartMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "branch",name="branch")
    private Branch branch= new Branch();

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("currentBranch: " + GitUtils.getCurrentBranch());
        getLog().info("Current version: " + project.getVersion());

        SemanticVersion currentVersion = SemanticVersion.parse(project.getVersion());

        // Use Optional to provide a default value if branch name is null or blank
        String branchName = Optional.ofNullable(branch.getName())
                .filter(name -> !name.isBlank())
                .orElse("123456");

        String preRelease = String.format("%s-%s-%s",
                currentVersion.getPreRelease(),
                BranchType.FEATURE.getValue(),
                branchName
        );

        SemanticVersion featVersion = new SemanticVersion(
                currentVersion.getMajor(),
                currentVersion.getMinor(),
                currentVersion.getPatch(),
                preRelease,
                currentVersion.getBuild()
        );

        getLog().info("Feature version: " + featVersion);
    }

}
