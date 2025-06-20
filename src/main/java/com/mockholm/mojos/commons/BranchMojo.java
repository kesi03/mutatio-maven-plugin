package com.mockholm.mojos.commons;

import com.mockholm.commands.GitCommand;
import com.mockholm.commands.PomCommand;
import com.mockholm.config.BranchAction;
import com.mockholm.config.BranchType;
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

public class BranchMojo {
    private final MojoCommons commons;

    public BranchMojo(MojoCommons commons){
        this.commons=commons;
    }

    public void executeStart(@NotNull BranchType branchType){
        commons.getLog().info("currentBranch: " + GitUtils.getCurrentBranch());
        commons.getLog().info("Current version: " + commons.getProject().getVersion());

        SemanticVersion currentVersion = SemanticVersion.parse(commons.getProject().getVersion());

        // Use Optional to provide a default value if branch name is null or blank
        String branchName = Optional.ofNullable(commons.getBranch().getName())
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


        commons.getLog().info("CHORE version: " + featVersion.toString());

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
            PomCommand pomCommand = new PomCommand(commons.getBaseDir(), commons.getLog());
            new GitCommand(commons.getLog())
                    .changeBranch(BranchType.DEVELOPMENT.getValue())
                    .changeBranch(branchType.getValue()+"/"+branchName)
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
                    .close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    
    public void executeEnd(@NotNull BranchType branchType){
        commons.getLog().info("currentBranch: " + GitUtils.getCurrentBranch());
        commons.getLog().info("Current version: " + commons.getProject().getVersion());

        SemanticVersion currentVersion = SemanticVersion.parse(commons.getProject().getVersion());

        // Use Optional to provide a default value if branch name is null or blank
        String branchName = Optional.ofNullable(commons.getBranch().getName())
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


        commons.getLog().info("CHORE version: " + featVersion.toString());

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
            PomCommand pomCommand = new PomCommand(commons.getBaseDir(), commons.getLog());
            AtomicReference<String> developmentVersion= new AtomicReference<>("");
            new GitCommand(commons.getLog())
                    .changeBranch(BranchType.DEVELOPMENT.getValue())
                    .runPomCommands(cmd -> {
                        developmentVersion.set(PomUtils.getVersion(commons.getBaseDir()));
                        commons.getLog().info("version: "+developmentVersion);

                    }, pomCommand)
                    .changeBranch(branchType.getValue()+"/"+branchName)
                    .gitInfo()
                    .runPomCommands(cmd -> {
                        commons.getLog().info("dev: "+developmentVersion);
                        commons.getLog().info("version: "+PomUtils.getVersion(commons.getBaseDir()));
                    }, pomCommand)
                    .mergeBranches(branchType.getValue()+"/"+branchName,BranchType.DEVELOPMENT.getValue())
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
