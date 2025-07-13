package com.mockholm.mojos.commons;

import com.mockholm.commands.GitCommand;
import com.mockholm.commands.PomCommand;
import com.mockholm.commands.ShellCommand;
import com.mockholm.config.*;
import com.mockholm.models.CommitDescription;
import com.mockholm.models.MojoCommons;
import com.mockholm.models.ConventionalCommit;
import com.mockholm.utils.CommitUtils;
import com.mockholm.utils.GitUtils;
import com.mockholm.utils.SemanticVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ReleaseMojo {
    private final MojoCommons commons;

    public ReleaseMojo(MojoCommons commons) {
        this.commons = commons;
    }

    public void executeStart(@NotNull ReleaseType releaseType, VersionIdentifier versionIdentifier) {
        commons.getLog().info("Current Branch Name: " + GitUtils.getCurrentBranch());
        commons.getLog().info("Current version: " + commons.getProject().getVersion());

        SemanticVersion currentVersion = SemanticVersion.parse(commons.getProject().getVersion());

        commons.getLog().info("Current Branch Version: " + currentVersion.toString());

        SemanticVersion nextVersion = getNextVersion(currentVersion, releaseType);

        commons.getLog().info("Release Branch: " + nextVersion.toString());

        SemanticVersion nextDevelopmentVersion = getNextVersion(currentVersion, releaseType,
                VersionIdentifier.SNAPSHOT.getValue(), "");

        commons.getLog().info("Dev Branch: " + nextDevelopmentVersion.toString());

        AtomicReference<String> commitMessage = new AtomicReference<>("");

        try {
            GitConfiguration gitConfiguration = new GitConfiguration()
                    .withServerKey(commons.getProject().getProperties().getProperty("gitProvider"))
                    .withScm(commons.getProject().getScm())
                    .withSettings(commons.getSettings());
            String baseDir = commons.getProject().getBasedir().getAbsolutePath();

            PomCommand pomCommand = new PomCommand(baseDir, commons.getLog());

            String releaseBranch = BranchType.RELEASE.getValue() + "/" + nextVersion.toString();
            String releaseTag = BranchType.RELEASE.getValue() + "-" + nextVersion.toString();

            new GitCommand(commons.getLog())
                    .changeBranch(BranchType.DEVELOPMENT.getValue(), gitConfiguration)
                    .changeBranch(releaseBranch, gitConfiguration)
                    .gitInfo()
                    .runPomCommands(cmd -> {
                        try {
                            pomCommand
                                    .setVersion(nextVersion.toString())
                                    .updatePomVersion()
                                    .updateModules();

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
                            commitMessage.set(CommitUtils.format(commit));
                            commons.getLog().info("Commit: " + commitMessage);
                        } catch (MojoExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }, pomCommand)
                    .addAllChanges()
                    .commit(commitMessage.get())
                    .push(gitConfiguration)
                    .changeBranch(BranchType.DEVELOPMENT.getValue(), gitConfiguration)
                    .runPomCommands(cmd -> {
                        try {
                            pomCommand
                                    .setVersion(nextDevelopmentVersion.toString())
                                    .updatePomVersion()
                                    .updateModules();

                            CommitDescription description = new CommitDescription.Builder()
                                    .action(BranchAction.START)
                                    .branchName(BranchType.DEVELOPMENT.getValue())
                                    .message("branch...")
                                    .build();

                            ConventionalCommit commit = new ConventionalCommit.Builder()
                                    .type(BranchType.DEVELOPMENT)
                                    .scope("")
                                    .description(description.toString())
                                    .isBreaking(false)
                                    .body("")
                                    .footer("")
                                    .build();
                            commitMessage.set(CommitUtils.format(commit));
                            commons.getLog().info("Commit: " + commitMessage);
                        } catch (MojoExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }, pomCommand)
                    .addAllChanges()
                    .commit(commitMessage.get())
                    .gitInfo()
                    .push(gitConfiguration)
                    .runShellCommands(cmd -> {
                        List<String[]> properties = Arrays.asList(
                                new String[] { "MUTATIO_NEXT_DEV_VERSION", nextDevelopmentVersion.toString() },
                                new String[] { "MUTATIO_NEXT_RELEASE_VERSION", nextVersion.toString() });
                        cmd.setBuildProperties(properties);
                    }, new ShellCommand(commons.getLog()))
                    .close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public SemanticVersion getNextVersion(SemanticVersion currentVersion, ReleaseType releaseType) {
        return getNextVersion(currentVersion, releaseType, null, null);
    }

    public SemanticVersion getNextVersion(SemanticVersion currentVersion, ReleaseType releaseType, String preRelease,
            String build) {
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
                build != null ? build : "");
    }

    public void executeEnd(@NotNull String release, BranchType mainOrMaster) {
        commons.getLog().info("executeEnd");
        commons.getLog().info("currentBranch: " + GitUtils.getCurrentBranch());
        commons.getLog().info("Current version: " + commons.getProject().getVersion());

        SemanticVersion currentVersion = SemanticVersion.parse(commons.getProject().getVersion());

        commons.getLog().info("Current version: " + currentVersion.toString());

        SemanticVersion releaseVersion = SemanticVersion.parse(release);

        commons.getLog().info("Release version: " + releaseVersion.toString());

        String releaseBranch = "release/" + releaseVersion.toString();

        String releaseTag = "release-"+releaseVersion.toString();

        String baseDir = commons.getProject().getBasedir().getAbsolutePath();

        PomCommand pomCommand = new PomCommand(baseDir, commons.getLog());

        commons.getLog().info("mainOrMaster: " + mainOrMaster.getValue());

        AtomicReference<String> commitMessage = new AtomicReference<>("");

        try {
            GitConfiguration gitConfiguration = new GitConfiguration()
                    .withServerKey(commons.getProject().getProperties().getProperty("gitProvider"))
                    .withScm(commons.getProject().getScm())
                    .withSettings(commons.getSettings());

            new GitCommand(commons.getLog())
                    .changeBranch(releaseBranch, gitConfiguration)
                    .gitInfo()
                    .runPomCommands(cmd -> {
                        CommitDescription description = new CommitDescription.Builder()
                                .action(BranchAction.FINISH)
                                .branchName(releaseVersion.toString())
                                .message("branch...")
                                .build();

                        ConventionalCommit commit = new ConventionalCommit.Builder()
                                .type(BranchType.RELEASE)
                                .scope(releaseVersion.toString())
                                .description(description.toString())
                                .isBreaking(false)
                                .body("")
                                .footer("")
                                .build();
                        commitMessage.set(CommitUtils.format(commit));
                        commons.getLog().info("Commit: " + commitMessage);
                    }, pomCommand)
                    .addAllChanges()
                    .commit(commitMessage.get())
                    .mergeBranches(releaseBranch, mainOrMaster.getValue(),gitConfiguration)
                    .createTag(releaseTag)
                    .pushTag(releaseTag,gitConfiguration)
                    .changeBranch(mainOrMaster.getValue(), gitConfiguration)
                    .gitInfo()
                    .push(gitConfiguration)
                    .runShellCommands(cmd -> {
                        List<String[]> properties = Arrays.asList(
                                new String[] { "MUTATIO_RELEASE_BRANCH", releaseBranch },
                                new String[] { "MUTATIO_RELEASE_TAG", releaseTag },
                                new String[] { "MUTATIO_RELEASE_VERSION", releaseVersion.toString() });
                        cmd.setBuildProperties(properties);
                    }, new ShellCommand(commons.getLog()))
                    .close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
