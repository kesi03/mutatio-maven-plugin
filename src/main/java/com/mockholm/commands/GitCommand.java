package com.mockholm.commands;

import com.mockholm.config.BranchType;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class GitCommand {
    private final Git git;
    private final Log log;


    public GitCommand() throws IOException {
        this.git = Git.open(new File("."));
        this.log = null;
    }

    public GitCommand(String gitPath) throws IOException {
        String resolvedPath = Optional.ofNullable(gitPath)
                .filter(path -> !path.isBlank())
                .orElse(".");

        this.git = Git.open(new File(resolvedPath));
        this.log = null;
    }

    public GitCommand(Log log) throws IOException {
        this.git = Git.open(new File("."));
        this.log = log;
    }

    public GitCommand(Log log,String gitPath) throws IOException {
        String resolvedPath = Optional.ofNullable(gitPath)
                .filter(path -> !path.isBlank())
                .orElse(".");

        this.git = Git.open(new File(resolvedPath));
        this.log = log;
    }

    private void info(String msg) {
        if (log != null) log.info(msg);
        else System.out.println(msg);
    }

    private void warn(String msg) {
        if (log != null) log.warn(msg);
        else System.out.println("WARN: " + msg);
    }

    private void error(String msg, Throwable t) {
        if (log != null) log.error(msg, t);
        else {
            System.err.println("ERROR: " + msg);
            t.printStackTrace(System.err);
        }
    }

    public GitCommand gitInfo(){
        String branch="";
        Git git = null;
        try {
            git = Git.open(new File("."));
            branch = git.getRepository().getBranch();
            log.info("current branch is: "+branch);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public GitCommand changeBranch(String targetBranch) {
        try {
            String currentBranch = git.getRepository().getBranch();

            if (Objects.equals(currentBranch, targetBranch)) {
                info("Already on branch '" + targetBranch + "'.");
                return this;
            }

            boolean localExists = git.branchList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + targetBranch));

            if (localExists) {
                git.checkout().setName(targetBranch).call();
                info("Switched to existing local branch '" + targetBranch + "'.");
                return this;
            }

            boolean remoteExists = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/remotes/origin/" + targetBranch));

            if (remoteExists) {
                git.checkout()
                        .setCreateBranch(true)
                        .setName(targetBranch)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .setStartPoint("origin/" + targetBranch)
                        .call();
                info("Created and switched to branch '" + targetBranch + "' tracking origin.");
            } else {
                git.checkout()
                        .setCreateBranch(true)
                        .setName(targetBranch)
                        .call();
                info("Created and switched to new local branch '" + targetBranch + "'.");
            }
        } catch (IOException | GitAPIException e) {
            error("Failed to change branch to '" + targetBranch + "'", e);
            throw new RuntimeException("Failed to change branch", e);
        }
        return this;
    }

    public GitCommand createBranch(String branchName) {
        try {
            boolean localExists = git.branchList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + branchName));

            if (localExists) {
                info("Branch '" + branchName + "' already exists locally.");
                return this;
            }

            boolean remoteExists = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/remotes/origin/" + branchName));

            if (remoteExists) {
                git.checkout()
                        .setCreateBranch(true)
                        .setName(branchName)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .setStartPoint("origin/" + branchName)
                        .call();
                info("Created and checked out branch '" + branchName + "' tracking origin.");
            } else {
                git.checkout()
                        .setCreateBranch(true)
                        .setName(branchName)
                        .call();
                info("Created new local branch '" + branchName + "'.");
            }
        } catch (GitAPIException e) {
            error("Failed to create branch: " + branchName, e);
            throw new RuntimeException("Failed to create branch: " + branchName, e);
        }
        return this;
    }

    public GitCommand pushBranch() {
        try {
            String currentBranch = git.getRepository().getBranch();
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(new RefSpec(currentBranch + ":" + currentBranch))
                    .call();
            info("Pushed local branch '" + currentBranch + "' to origin.");
        } catch (IOException | GitAPIException e) {
            error("Failed to push branch", e);
            throw new RuntimeException("Failed to push branch", e);
        }
        return this;
    }

    public GitCommand createTag(String tag) {
        try {
            git.tag()
                    .setName(tag)
                    .setMessage("Created tag: " + tag)
                    .call();
            info("Tag '" + tag + "' created locally.");
        } catch (GitAPIException e) {
            error("Failed to create tag: " + tag, e);
            throw new RuntimeException("Failed to create tag: " + tag, e);
        }
        return this;
    }

    public GitCommand pushTag(String tag) {
        try {
            RefSpec tagRefSpec = new RefSpec("refs/tags/" + tag + ":refs/tags/" + tag);
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(tagRefSpec)
                    .call();
            info("Tag '" + tag + "' pushed to origin.");
        } catch (GitAPIException e) {
            error("Failed to push tag: " + tag, e);
            throw new RuntimeException("Failed to push tag: " + tag, e);
        }
        return this;
    }

    public GitCommand removeTag(String tag) {
        try {
            List<String> deleted = git.tagDelete()
                    .setTags(tag)
                    .call();

            if (deleted.isEmpty()) {
                warn("Tag '" + tag + "' not found locally.");
            } else {
                info("Tag '" + tag + "' deleted locally.");
            }

            RefSpec refSpec = new RefSpec()
                    .setSource(null)
                    .setDestination("refs/tags/" + tag);

            git.push()
                    .setRemote("origin")
                    .setRefSpecs(refSpec)
                    .call();

            info("Tag '" + tag + "' deleted from origin.");
        } catch (GitAPIException e) {
            error("Failed to remove tag: " + tag, e);
            throw new RuntimeException("Failed to remove tag: " + tag, e);
        }
        return this;
    }

    public boolean checkIfBranchExists(String branchName) {
        try {
            boolean localExists = git.branchList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + branchName));

            boolean remoteExists = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/remotes/origin/" + branchName));

            if (localExists) info("Branch '" + branchName + "' exists locally.");
            if (remoteExists) info("Branch '" + branchName + "' exists on origin.");
            if (!localExists && !remoteExists) warn("Branch '" + branchName + "' does not exist locally or remotely.");

            return localExists || remoteExists;
        } catch (GitAPIException e) {
            error("Failed to check branch existence: " + branchName, e);
            throw new RuntimeException("Failed to check branch existence: " + branchName, e);
        }
    }

    public boolean checkIfTagExists(String tagName) {
        try {
            boolean localExists = git.tagList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/tags/" + tagName));

            Collection<Ref> remoteTags = git.lsRemote()
                    .setTags(true)
                    .setRemote("origin")
                    .call();

            boolean remoteExists = remoteTags.stream()
                    .anyMatch(ref -> ref.getName().equals("refs/tags/" + tagName));

            if (localExists) info("Tag '" + tagName + "' exists locally.");
            if (remoteExists) info("Tag '" + tagName + "' exists on origin.");
            if (!localExists && !remoteExists) warn("Tag '" + tagName + "' does not exist locally or remotely.");

            return localExists || remoteExists;
        } catch (GitAPIException e) {
            error("Failed to check tag existence: " + tagName, e);
            throw new RuntimeException("Failed to check tag existence: " + tagName, e);
        }
    }

    public GitCommand addAllChanges() {
        try {
            git.add()
                    .addFilepattern(".")
                    .call();
            info("Staged all changes (equivalent to 'git add .').");
        } catch (GitAPIException e) {
            error("Failed to stage changes", e);
            throw new RuntimeException("Failed to stage changes", e);
        }
        return this;
    }

    public GitCommand add(@NotNull Optional<String> filePattern) {
        try {
            String pattern = filePattern.filter(p -> !p.isBlank()).orElse(".");
            git.add()
                    .addFilepattern(pattern)
                    .call();
            info("Staged changes for pattern: " + pattern);
        } catch (GitAPIException e) {
            error("Failed to stage changes for pattern: " + filePattern.orElse("."), e);
            throw new RuntimeException("Failed to stage changes", e);
        }
        return this;
    }

    public GitCommand commit(String message) {
        try {
            git.commit()
                    .setMessage(message)
                    .call();
            info("Committed changes with message: " + message);
        } catch (GitAPIException e) {
            error("Failed to commit changes", e);
            throw new RuntimeException("Failed to commit changes", e);
        }
        return this;
    }

    public GitCommand reset() {
        try {
            git.reset()
                    .setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD)
                    .call();
            info("Reset all changes (equivalent to 'git reset --hard').");
        } catch (GitAPIException e) {
            error("Failed to reset changes", e);
            throw new RuntimeException("Failed to reset changes", e);
        }
        return this;
    }

    public GitCommand reset(@NotNull Optional<String> filePattern) {
        try {
            String pattern = filePattern.filter(p -> !p.isBlank()).orElse(".");
            git.reset()
                    .setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD)
                    .addPath(pattern)
                    .call();
            info("Reset changes for pattern: " + pattern);
        } catch (GitAPIException e) {
            error("Failed to reset changes for pattern: " + filePattern.orElse("."), e);
            throw new RuntimeException("Failed to reset changes", e);
        }
        return this;
    }

    public GitCommand pull() {
        try {
            git.pull()
                    .setRemote("origin")
                    .call();
            info("Pulled changes from origin.");
        } catch (GitAPIException e) {
            error("Failed to pull changes", e);
            throw new RuntimeException("Failed to pull changes", e);
        }
        return this;
    }

    public GitCommand push() {
        try {
            git.push()
                    .setRemote("origin")
                    .call();
            info("Pushed changes to origin.");
        } catch (GitAPIException e) {
            error("Failed to push changes", e);
            throw new RuntimeException("Failed to push changes", e);
        }
        return this;
    }

    public GitCommand fetch() {
        try {
            git.fetch()
                    .setRemote("origin")
                    .call();
            info("Fetched changes from origin.");
        } catch (GitAPIException e) {
            error("Failed to fetch changes", e);
            throw new RuntimeException("Failed to fetch changes", e);
        }
        return this;
    }

    public GitCommand deleteBranch(String branchName) {
        try {
            git.branchDelete()
                    .setBranchNames(branchName)
                    .setForce(true)
                    .call();
            info("Deleted branch: " + branchName);
        } catch (GitAPIException e) {
            error("Failed to delete branch: " + branchName, e);
            throw new RuntimeException("Failed to delete branch", e);
        }
        return this;
    }

    public GitCommand deleteRemoteBranch(String branchName) {
        try {
            RefSpec refSpec = new RefSpec(":" + branchName);
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(refSpec)
                    .call();
            info("Deleted remote branch: " + branchName);
        } catch (GitAPIException e) {
            error("Failed to delete remote branch: " + branchName, e);
            throw new RuntimeException("Failed to delete remote branch", e);
        }
        return this;
    }

    public GitCommand deleteLocalTag(String tagName) {
        try {
            git.tagDelete()
                    .setTags(tagName)
                    .call();
            info("Deleted local tag: " + tagName);
        } catch (GitAPIException e) {
            error("Failed to delete local tag: " + tagName, e);
            throw new RuntimeException("Failed to delete local tag", e);
        }
        return this;
    }

    public GitCommand deleteRemoteTag(String tagName) {
        try {
            RefSpec refSpec = new RefSpec(":" + tagName);
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(refSpec)
                    .call();
            info("Deleted remote tag: " + tagName);
        } catch (GitAPIException e) {
            error("Failed to delete remote tag: " + tagName, e);
            throw new RuntimeException("Failed to delete remote tag", e);
        }
        return this;
    }

    public GitCommand close(){
        git.close();
        info("Closed git repository.");
        return this;
    }


    public GitCommand runPomCommands(@NotNull Consumer<PomCommand> pomCommandConsumer,PomCommand command){
        pomCommandConsumer.accept(command);
        return this;
    }

    public GitCommand mergeBranches(@NotNull String from, @NotNull String to) {
        try {
            // Checkout target branch (to)
            git.checkout().setName(to).call();
            info("Checked out target branch: " + to);

            // Merge source branch (from) into target
            MergeResult result = git.merge()
                    .include(git.getRepository().findRef(from))
                    .call();

            switch (result.getMergeStatus()) {
                case FAST_FORWARD:
                case MERGED:
                case MERGED_SQUASHED:
                    info("Merge successful: " + from + " → " + to);
                    break;
                default:
                    error("Merge failed with status: " + result.getMergeStatus(),null);
                    throw new RuntimeException("Merge failed: " + result.getMergeStatus());
            }
        } catch (GitAPIException | IOException e) {
            error("Error merging " + from + " into " + to, e);
            throw new RuntimeException("Failed to merge branches", e);
        }

        return this;
    }

    public GitCommand mergeBranchesWithExclusion(@NotNull String from, @NotNull String to, String exclusion) {
        try {
            // Checkout target branch (to)
            git.checkout().setName(to).call();
            info("Checked out target branch: " + to);

            // Merge source branch (from) into target
            MergeResult result = git.merge()
                    .include(git.getRepository().findRef(from))
                    .setCommit(false)
                    .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                    .call();

            switch (result.getMergeStatus()) {
                case FAST_FORWARD:
                case MERGED:
                case MERGED_SQUASHED:
                    info("Merge successful: " + from + " → " + to);
                    break;
                default:
                    error("Merge failed with status: " + result.getMergeStatus(),null);
                    throw new RuntimeException("Merge failed: " + result.getMergeStatus());
            }



        } catch (GitAPIException | IOException e) {
            error("Error merging " + from + " into " + to, e);
            throw new RuntimeException("Failed to merge branches", e);
        }

        return this;
    }



}