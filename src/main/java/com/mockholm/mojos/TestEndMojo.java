package com.mockholm.mojos;

import com.mockholm.commands.GitCommand;
import com.mockholm.commands.PomCommand;
import com.mockholm.config.Branch;
import com.mockholm.config.BranchAction;
import com.mockholm.config.BranchType;
import com.mockholm.models.CommitDescription;
import com.mockholm.models.ConventionalCommit;
import com.mockholm.utils.CommitUtils;
import com.mockholm.utils.GitUtils;
import com.mockholm.utils.PomUtils;
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
import java.util.concurrent.atomic.AtomicReference;

@Mojo(name = "chore-end", aggregator = true, defaultPhase = LifecyclePhase.NONE)
public class TestEndMojo extends AbstractMojo {

        @Parameter(defaultValue = "${project}", required = true, readonly = true)
        private MavenProject project;

        @Parameter(defaultValue = "${project.basedir}", readonly = true)
        private String baseDir;

        @Parameter(property = "branch", name = "branch")
        private Branch branch = new Branch();

        public void execute() throws MojoExecutionException, MojoFailureException {
                getLog().info("currentBranch: " + GitUtils.getCurrentBranch());
                getLog().info("Current version: " + project.getVersion());

                SemanticVersion currentVersion = SemanticVersion.parse(project.getVersion());

                // Use Optional to provide a default value if branch name is null or blank
                String branchName = Optional.ofNullable(branch.getName())
                                .filter(name -> !name.isBlank())
                                .orElse("123456");

                String branchFullname =BranchType.CHORE.getValue()+"-"+branchName;

                getLog().info("Branch: "+branchFullname);

                String preRelease = String.format("%s-%s-%s",
                                currentVersion.getPreRelease(),
                                BranchType.CHORE.getUppercaseValue(),
                                branchName);

                SemanticVersion featVersion = new SemanticVersion(
                                currentVersion.getMajor(),
                                currentVersion.getMinor(),
                                currentVersion.getPatch(),
                                preRelease,
                                currentVersion.getBuild());


                getLog().info("CHORE version: " + featVersion.toString());

                CommitDescription description = new CommitDescription.Builder()
                                .action(BranchAction.FINISH)
                                .branchName(branchName)
                                .message("branch...")
                                .build();

                ConventionalCommit commit = new ConventionalCommit.Builder()
                                .type(BranchType.CHORE)
                                .scope(branchName)
                                .description(description.toString())
                                .isBreaking(false)
                                .body("")
                                .footer("")
                                .build();

                String commitMessage = CommitUtils.format(commit);

                getLog().info("Commit: " + commitMessage);

                try {
                        PomCommand pomCommand = new PomCommand(baseDir, getLog());
                        AtomicReference<String> developmentVersion= new AtomicReference<>("");
                        new GitCommand(getLog())
                                .changeBranch(BranchType.DEVELOPMENT.getValue())
                                .runPomCommands(cmd -> {
                                        developmentVersion.set(PomUtils.getVersion(baseDir));
                                        getLog().info("version: "+developmentVersion);

                                }, pomCommand)
                                .changeBranch(BranchType.CHORE.getValue()+"/"+branchName)
                                .gitInfo()
                                .runPomCommands(cmd -> {
                                        getLog().info("dev: "+developmentVersion);
                                        getLog().info("version: "+PomUtils.getVersion(baseDir));
                                }, pomCommand)
                                .mergeBranches(BranchType.CHORE.getValue()+"/"+branchName,BranchType.DEVELOPMENT.getValue())
                                .changeBranch(BranchType.DEVELOPMENT.getValue())
                                .runPomCommands(cmd -> {
                                        try {
                                                pomCommand
                                                        .setVersion(developmentVersion.get())
                                                        .updatePomVersion()
                                                        .updateModules();
                                        } catch (MojoExecutionException e) {
                                                throw new RuntimeException(e);
                                        }
                                }, pomCommand)
                                .addAllChanges()
                                .commit(commitMessage)
                                .gitInfo()
                                        .close();

                } catch (IOException e) {
                        throw new RuntimeException(e);
                }

        }

}
