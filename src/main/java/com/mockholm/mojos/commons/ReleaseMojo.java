package com.mockholm.mojos.commons;

import com.mockholm.commands.GitCommand;
import com.mockholm.commands.PomCommand;
import com.mockholm.config.BranchAction;
import com.mockholm.config.BranchType;
import com.mockholm.config.ReleaseType;
import com.mockholm.config.VersionIdentifier;
import com.mockholm.models.CommitDescription;
import com.mockholm.models.Commons;
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

public class ReleaseMojo {
    private final Commons commons;

    public ReleaseMojo(Commons commons){
        this.commons=commons;
    }

    public void executeStart(@NotNull ReleaseType releaseType, VersionIdentifier versionIdentifier){
        commons.getLog().info("currentBranch: " + GitUtils.getCurrentBranch());
        commons.getLog().info("Current version: " + commons.getProject().getVersion());

        SemanticVersion currentVersion = SemanticVersion.parse(commons.getProject().getVersion());

        commons.getLog().info("Branch: "+currentVersion.toString());

        SemanticVersion nextVersion = getNextVersion(currentVersion,releaseType);

        String version =BranchType.RELEASE.getValue()+"-"+nextVersion.toString();

        commons.getLog().info("Branch: "+version);

        SemanticVersion nextDevelopmentVersion = getNextVersion(currentVersion,releaseType,versionIdentifier.getValue(),"");

        String devVersion =BranchType.RELEASE.getValue()+"-"+nextVersion.toString();

        commons.getLog().info("Dev Branch: "+devVersion);


        CommitDescription description = new CommitDescription.Builder()
                .action(BranchAction.START)
                .branchName(nextVersion.toString())
                .message("branch...")
                .build();

        ConventionalCommit commit = new ConventionalCommit.Builder()
                .type(BranchType.RELEASE)
                .scope(nextVersion.toString())
                .description(description.toString())
                .isBreaking(false)
                .body("")
                .footer("")
                .build();

        String commitMessage = CommitUtils.format(commit);

        commons.getLog().info("Commit: " + commitMessage);

//        try {
//            PomCommand pomCommand = new PomCommand(commons.getBaseDir(), commons.getLog());
//            new GitCommand(commons.getLog())
//                    .changeBranch(BranchType.DEVELOPMENT.getValue())
//                    .changeBranch(BranchType.RELEASE.getValue()+"/"+nextVersion.toString())
//                    .gitInfo()
//                    .runPomCommands(cmd -> {
//                        try {
//                            pomCommand
//                                    .setVersion(nextVersion.toString())
//                                    .updatePomVersion()
//                                    .updateModules();
//                        } catch (MojoExecutionException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }, pomCommand)
//                    .addAllChanges()
//                    .commit(commitMessage)
//                    .gitInfo()
//                    .close();
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

    }

    public SemanticVersion getNextVersion(SemanticVersion currentVersion, ReleaseType releaseType) {
        return getNextVersion(currentVersion, releaseType, null, null);
    }

    public SemanticVersion getNextVersion(SemanticVersion currentVersion, ReleaseType releaseType, String preRelease, String build) {
        int major = currentVersion.getMajor();
        int minor = currentVersion.getMinor();
        int patch = currentVersion.getPatch();

        switch (releaseType) {
            case MAJOR:
                major += 1;
                break;
            case MINOR:
                minor += 1;
                break;
            case PATCH:
                patch += 1;
                break;
            default:
                return currentVersion;
        }

        return new SemanticVersion(
                major,
                minor,
                patch,
                preRelease != null ? preRelease : "",
                build != null ? build : ""
        );
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
