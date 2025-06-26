package com.mockholm.mojos.commons;

import com.mockholm.commands.GitCommand;
import com.mockholm.commands.PomCommand;
import com.mockholm.config.BranchAction;
import com.mockholm.config.BranchType;
import com.mockholm.config.GitConfiguration;
import com.mockholm.models.CommitDescription;
import com.mockholm.models.MojoCommons;
import com.mockholm.models.ConventionalCommit;
import com.mockholm.utils.CommitUtils;
import com.mockholm.utils.GitUtils;
import com.mockholm.utils.PomUtils;
import com.mockholm.utils.SemanticVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Common methods used when creating branches such as feat,chore,fix ect.
 */
public class BranchMojo {
    private final MojoCommons commons;

    public BranchMojo(MojoCommons commons){
        this.commons=commons;
    }

    /**
     * Start will create a new branch such as feat/jira-123456
     * @param branchType
     */
    public void executeStart(@NotNull BranchType branchType){
        commons.getLog().info("currentBranch: " + GitUtils.getCurrentBranch());
        commons.getLog().info("Current version: " + commons.getProject().getVersion());

        SemanticVersion currentVersion = SemanticVersion.parse(commons.getProject().getVersion());

        // Use Optional to provide a default value if branch name is null or blank
        String branchName = Optional.ofNullable(commons.getRepoIdentity())
                .filter(name -> !name.isBlank())
                .orElse("123456");

        String branchFullname =branchType.getValue()+"-"+branchName;

        commons.getLog().info("Branch: "+branchFullname);

        String preRelease = String.format("%s-%s-%s",
                currentVersion.getPreRelease(),
                branchType.getUppercaseValue(),
                branchName);

        SemanticVersion featVersion = new SemanticVersion(
                currentVersion.getMajor(),
                currentVersion.getMinor(),
                currentVersion.getPatch(),
                preRelease,
                currentVersion.getBuild());


        commons.getLog().info(branchType+" version: " + featVersion.toString());

        CommitDescription description = new CommitDescription.Builder()
                .action(BranchAction.START)
                .branchName(branchName)
                .message("branch...")
                .build();

        ConventionalCommit commit = new ConventionalCommit.Builder()
                .type(branchType)
                .scope(branchName)
                .description(description.toString())
                .isBreaking(false)
                .body("")
                .footer("")
                .build();

        String commitMessage = CommitUtils.format(commit);

        commons.getLog().info("Commit: " + commitMessage);

        try {

            GitConfiguration gitConfiguration=new GitConfiguration()
                    .withServerKey(commons.getProject().getProperties().getProperty("gitProvider"))
                    .withScm(commons.getProject().getScm())
                    .withSettings(commons.getSettings());
            String baseDir = commons.getProject().getBasedir().getAbsolutePath();

            PomCommand pomCommand = new PomCommand(baseDir, commons.getLog());
            new GitCommand(commons.getLog())
                    .changeBranch(BranchType.DEVELOPMENT.getValue(),gitConfiguration)
                    .changeBranch(branchType.getValue()+"/"+branchName,gitConfiguration)
                    .gitInfo()
                    .runPomCommands(cmd -> {
                        try {
                            pomCommand
                                    .setVersion(featVersion.toString())
                                    .updatePomVersion()
                                    .updateModules();
                        } catch (MojoExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }, pomCommand)
                    .addAllChanges()
                    .commit(commitMessage)
                    .gitInfo()
                    .push(gitConfiguration)
                    .close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * End will merge the branch to develop
     * @param branchType
     */
    public void executeEnd(@NotNull BranchType branchType){
        commons.getLog().info("currentBranch: " + GitUtils.getCurrentBranch());
        commons.getLog().info("Current version: " + commons.getProject().getVersion());

        SemanticVersion currentVersion = SemanticVersion.parse(commons.getProject().getVersion());

        // Use Optional to provide a default value if branch name is null or blank
        String branchName = Optional.ofNullable(commons.getRepoIdentity())
                .filter(name -> !name.isBlank())
                .orElse("123456");

        String branchFullName =branchType.getValue()+"-"+branchName;

        commons.getLog().info("Branch: "+branchFullName);

        String preRelease = String.format("%s-%s-%s",
                currentVersion.getPreRelease(),
                branchType.getUppercaseValue(),
                branchName);

        SemanticVersion featVersion = new SemanticVersion(
                currentVersion.getMajor(),
                currentVersion.getMinor(),
                currentVersion.getPatch(),
                preRelease,
                currentVersion.getBuild());


        commons.getLog().info( branchType.getValue()+" version: " + featVersion.toString());

        CommitDescription description = new CommitDescription.Builder()
                .action(BranchAction.FINISH)
                .branchName(branchName)
                .message("branch...")
                .build();

        ConventionalCommit commit = new ConventionalCommit.Builder()
                .type(branchType)
                .scope(branchName)
                .description(description.toString())
                .isBreaking(false)
                .body("")
                .footer("")
                .build();

        String commitMessage = CommitUtils.format(commit);

        commons.getLog().info("Commit: " + commitMessage);

        try {
            GitConfiguration gitConfiguration=new GitConfiguration()
                    .withServerKey(commons.getProject().getProperties().getProperty("gitProvider"))
                    .withScm(commons.getProject().getScm())
                    .withSettings(commons.getSettings());
            
            String baseDir = commons.getProject().getBasedir().getAbsolutePath();

            PomCommand pomCommand = new PomCommand(baseDir, commons.getLog());
            AtomicReference<String> developmentVersion= new AtomicReference<>("");
            new GitCommand(commons.getLog())
                    .changeBranch(BranchType.DEVELOPMENT.getValue(),gitConfiguration)
                    .runPomCommands(cmd -> {
                        developmentVersion.set(PomUtils.getVersion(baseDir));
                        commons.getLog().info("version: "+developmentVersion);

                    }, pomCommand)
                    .changeBranch(branchType.getValue()+"/"+branchName,gitConfiguration)
                    .gitInfo()
                    .runPomCommands(cmd -> {
                        commons.getLog().info("dev: "+developmentVersion);
                        commons.getLog().info("version: "+PomUtils.getVersion(baseDir));
                    }, pomCommand)
                    .mergeBranches(branchType.getValue()+"/"+branchName,BranchType.DEVELOPMENT.getValue())
                    .changeBranch(BranchType.DEVELOPMENT.getValue(),gitConfiguration)
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
                    .push(gitConfiguration)
                    .close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
