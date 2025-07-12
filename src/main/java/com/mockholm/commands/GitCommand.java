package com.mockholm.commands;

import com.mockholm.config.GitConfiguration;
import com.mockholm.utils.GitCredentialUtils;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.mockholm.utils.GitCredentialUtils.SSH_REMOTE;

/**
 * A utility class that wraps common Git operations using the JGit API.
 * This class provides high-level methods for interacting with Git repositories,
 * including staging, committing, branching, merging, and managing tags.
 * <p>
 * Supports both HTTPS and SSH authentication flows where applicable.
 */
public class GitCommand {
    private final Git git;
    private final Log log;

    private boolean shouldSkipNext = false;

    public GitCommand skipNext() {
        this.shouldSkipNext = true;
        return this;
    }

    public boolean shouldSkip() {
        return this.shouldSkipNext;
    }


    /**
     * Creates a GitCommand instance using the current working directory as the repository path.
     *
     * @throws IOException if the Git repository cannot be opened from the current directory
     */
    public GitCommand() throws IOException {
        this.git = Git.open(new File("."));
        this.log = null;
    }

    /**
     * Creates a GitCommand instance using the specified path to the Git repository.
     * If the path is null or blank, defaults to the current working directory.
     *
     * @param gitPath the path to the Git repository, or blank to use the current directory
     * @throws IOException if the Git repository cannot be opened from the given path
     */
    public GitCommand(String gitPath) throws IOException {
        String resolvedPath = Optional.ofNullable(gitPath)
                .filter(path -> !path.isBlank())
                .orElse(".");
        this.git = Git.open(new File(resolvedPath));
        this.log = null;
    }

    /**
     * Creates a GitCommand instance using the current working directory
     * and the specified logger for output.
     *
     * @param log the logger instance for reporting messages
     * @throws IOException if the Git repository cannot be opened from the current directory
     */
    public GitCommand(Log log) throws IOException {
        this.git = Git.open(new File("."));
        this.log = log;
    }

    /**
     * Creates a GitCommand instance using the specified path to the Git repository
     * and the provided logger for output. If the path is null or blank, defaults
     * to the current working directory.
     *
     * @param log the logger instance for reporting messages
     * @param gitPath the path to the Git repository; blank or null defaults to current directory
     * @throws IOException if the Git repository cannot be opened from the given path
     */
    public GitCommand(Log log, String gitPath) throws IOException {
        String resolvedPath = Optional.ofNullable(gitPath)
                .filter(path -> !path.isBlank())
                .orElse(".");
        this.git = Git.open(new File(resolvedPath));
        this.log = log;
    }

    /**
     * Logs an informational message using the configured logger,
     * or prints to standard output if no logger is available.
     *
     * @param msg the message to log
     */
    private void info(String msg) {
        if (log != null) log.info(msg);
        else System.out.println(msg);
    }

    /**
     * Logs a warning message using the configured logger,
     * or prints with a "WARN:" prefix to standard output if no logger is available.
     *
     * @param msg the warning message to log
     */
    private void warn(String msg) {
        if (log != null) log.warn(msg);
        else System.out.println("WARN: " + msg);
    }

    /**
     * Logs an error message and associated throwable using the configured logger,
     * or prints to standard error if no logger is available.
     *
     * @param msg the error message to log
     * @param t the throwable to include in the log output
     */
    private void error(String msg, Throwable t) {
        if (log != null) log.error(msg, t);
        else {
            System.err.println("ERROR: " + msg);
            t.printStackTrace(System.err);
        }
    }

    /**
     * Opens the current Git repository and logs the name of the currently checked-out branch.
     *
     * @return this GitCommand instance
     * @throws RuntimeException if the repository cannot be opened or the branch cannot be determined
     */
    public GitCommand gitInfo() {
        String branch = "";
        Git git = null;
        try {
            git = Git.open(new File("."));
            branch = git.getRepository().getBranch();
            log.info("current branch is: " + branch);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }


    /**
     * Changes the current Git branch to the specified target branch
     * @param targetBranch  the name of the branch to switch to
     * @param configuration the git configuration needed for credentials
     * @return GitCommand
     */
    public GitCommand changeBranch(String targetBranch,GitConfiguration configuration) {
        if(GitCredentialUtils.isSSH(configuration.getScm())){
            info("using ssh");
            return changeBranch(targetBranch,transport -> {
                if (transport instanceof SshTransport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                }
            });
        }else{
            info("using credentials");
            CredentialsProvider credentialsProvider=GitCredentialUtils.getUserProvider(configuration.getSettings().getServer(configuration.getServerKey()).getPassword());
            return changeBranch(targetBranch,credentialsProvider);
        }
    }

    /**
      using HTTPS authentication.
     * If the branch exists locally, it switches directly. If not, it attempts to fetch from origin.
     * If the branch exists remotely, it is created locally and tracked. Otherwise, a new local-only
     * branch is created.
     *
     * @param targetBranch the name of the branch to switch to
     * @param credentialsProvider the credentials provider for remote access (HTTPS)
     * @return this GitCommand instance
     * @throws RuntimeException if an I/O or Git operation fails
     */
    public GitCommand changeBranch(String targetBranch, CredentialsProvider credentialsProvider) {
        try {
            String currentBranch = git.getRepository().getBranch();

            if (Objects.equals(currentBranch, targetBranch)) {
                info("Already on branch '" + targetBranch + "'.");
                return this;
            }

            boolean localExists = git.branchList().call().stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + targetBranch));

            if (localExists) {
                git.checkout().setName(targetBranch).call();
                info("Switched to existing local branch '" + targetBranch + "'.");
                return this;
            }

            git.fetch()
                    .setRemote("origin")
                    .setCredentialsProvider(credentialsProvider)
                    .call();

            boolean remoteExists = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/remotes/origin/" + targetBranch));

            if (remoteExists) {
                git.fetch().setCredentialsProvider(credentialsProvider).call();
                git.checkout()
                        .setCreateBranch(true)
                        .setName(targetBranch)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .setStartPoint("origin/" + targetBranch)
                        .call();
                info("Created and switched to branch '" + targetBranch + "' tracking origin.");
            } else {
                git.checkout().setCreateBranch(true).setName(targetBranch).call();
                info("Created and switched to new local branch '" + targetBranch + "'.");
            }
        } catch (IOException | GitAPIException e) {
            error("Failed to change branch to '" + targetBranch + "'", e);
            throw new RuntimeException("Failed to change branch", e);
        }
        return this;
    }

    /**
     * Changes the current Git branch to the specified target branch using SSH authentication.
     * If the branch exists locally, it switches directly. If not, it attempts to fetch from origin.
     * If the branch exists remotely, it is created locally and tracked. Otherwise, a new local-only
     * branch is created.
     *
     * @param targetBranch the name of the branch to switch to
     * @param sshCallback the SSH transport configuration callback for remote access
     * @return this GitCommand instance
     * @throws RuntimeException if an I/O or Git operation fails
     */
    public GitCommand changeBranch(String targetBranch, TransportConfigCallback sshCallback) {
        try {
            String currentBranch = git.getRepository().getBranch();

            if (Objects.equals(currentBranch, targetBranch)) {
                info("Already on branch '" + targetBranch + "'.");
                return this;
            }

            boolean localExists = git.branchList().call().stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + targetBranch));

            if (localExists) {
                git.checkout().setName(targetBranch).call();
                info("Switched to existing local branch '" + targetBranch + "'.");
                return this;
            }

            String remoteUrl = GitCredentialUtils.getRemoteUrl(git);

            info("remoteUrl: "+remoteUrl);

            GitCredentialUtils.addSSHRemote(git);

            git.fetch()
                    .setRemote(SSH_REMOTE)
                    .setTransportConfigCallback(sshCallback)
                    .call();

            boolean remoteExists = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/remotes/origin/" + targetBranch));

            if (remoteExists) {
                git.fetch().setRemote(SSH_REMOTE).setTransportConfigCallback(sshCallback).call();
                git.checkout()
                        .setCreateBranch(true)
                        .setName(targetBranch)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .setStartPoint("origin/" + targetBranch)
                        .call();
                info("Created and switched to branch '" + targetBranch + "' tracking origin.");
            } else {
                git.checkout().setCreateBranch(true).setName(targetBranch).call();
                info("Created and switched to new local branch '" + targetBranch + "'.");
            }
        } catch (IOException | GitAPIException e) {
            error("Failed to change branch to '" + targetBranch + "'", e);
            throw new RuntimeException("Failed to change branch", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Creates a new Git branch with the given name. If the branch exists remotely on origin,
     * it will be created locally and set to track the remote branch. If not, a local-only branch is created.
     *
     * @param branchName    the name of the branch to create
     * @param configuration the Git configuration containing authentication and server details
     * @return this GitCommand instance
     * @throws RuntimeException if the branch creation fails
     */
    public GitCommand createBranch(String branchName, GitConfiguration configuration) {

        if (GitCredentialUtils.isSSH(configuration.getScm())) {
            info("Using SSH to fetch branch data: " + branchName);
            return createBranch(branchName, transport -> {
                if (transport instanceof SshTransport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                }
            });
        } else {
            info("Using HTTPS credentials to fetch branch data: " + branchName);
            String password = configuration.getSettings()
                    .getServer(configuration.getServerKey())
                    .getPassword();
            CredentialsProvider credentialsProvider = GitCredentialUtils.getUserProvider(password);

            return createBranch(branchName,credentialsProvider);
        }
    }

    /**
     * Creates a new Git branch with the given name. If the branch exists remotely on origin,
     * it will be created locally and set to track the remote branch. If not, a local-only branch is created.
     *
     * @param branchName the name of the branch to create
     * @param credentialsProvider the credentials provider for remote operations (HTTPS)
     * @return this GitCommand instance
     * @throws RuntimeException if the branch creation fails
     */
    public GitCommand createBranch(String branchName, CredentialsProvider credentialsProvider) {
        try {
            boolean localExists = git.branchList().call().stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + branchName));

            if (localExists) {
                info("Branch '" + branchName + "' already exists locally.");
                return this;
            }

            git.fetch()
                    .setRemote("origin")
                    .setCredentialsProvider(credentialsProvider)
                    .call();

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

    /**
     * Creates a new Git branch with the given name. If the branch exists remotely on origin,
     * it will be created locally and set to track the remote branch. If not, a local-only branch is created.
     *
     * @param branchName the name of the branch to create
     * @param sshCallback the SSH transport configuration callback for authentication
     * @return this GitCommand instance
     * @throws RuntimeException if the branch creation fails
     */
    public GitCommand createBranch(String branchName, TransportConfigCallback sshCallback) {
        try {
            boolean localExists = git.branchList().call().stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + branchName));

            if (localExists) {
                info("Branch '" + branchName + "' already exists locally.");
                return this;
            }

            GitCredentialUtils.addSSHRemote(git);

            git.fetch()
                    .setRemote(SSH_REMOTE)
                    .setTransportConfigCallback(sshCallback)
                    .call();

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
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Pushes the current local branch to the origin remote using the appropriate authentication strategy.
     *
     * @param configuration the Git configuration containing authentication and server details
     * @return this GitCommand instance
     * @throws RuntimeException if the push operation fails
     */
    @SuppressWarnings("unused")
    public GitCommand pushBranch(GitConfiguration configuration) {
        try {
            String currentBranch = git.getRepository().getBranch();
            
            RefSpec branchRefSpec = new RefSpec(currentBranch + ":" + currentBranch);

            if (GitCredentialUtils.isSSH(configuration.getScm())) {
                info("Using SSH to push branch: " + currentBranch);

                return pushBranch(transport -> {
                    if (transport instanceof SshTransport) {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                    }
                });
            } else {
                info("Using HTTPS credentials to push branch: " + currentBranch);
                String password = configuration.getSettings()
                        .getServer(configuration.getServerKey())
                        .getPassword();
                CredentialsProvider credentialsProvider = GitCredentialUtils.getUserProvider(password);

                return pushBranch(credentialsProvider);
            }
        } catch (IOException e) {
            error("Failed to determine current branch", e);
            throw new RuntimeException("Failed to push current branch", e);
        }
    }

    /**
     * Pushes the current local branch to the origin remote using HTTPS authentication.
     *
     * @param credentialsProvider the credentials provider for remote access
     * @return this GitCommand instance
     * @throws RuntimeException if the push operation fails
     */
    public GitCommand pushBranch(CredentialsProvider credentialsProvider) {
        try {
            String currentBranch = git.getRepository().getBranch();
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(new RefSpec(currentBranch + ":" + currentBranch))
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            info("Pushed local branch '" + currentBranch + "' to origin.");
        } catch (IOException | GitAPIException e) {
            error("Failed to push branch", e);
            throw new RuntimeException("Failed to push branch", e);
        }
        return this;
    }

    /**
     * Pushes the current local branch to the origin remote using SSH authentication.
     *
     * @param sshCallback the SSH transport configuration callback
     * @return this GitCommand instance
     * @throws RuntimeException if the push operation fails
     */
    public GitCommand pushBranch(TransportConfigCallback sshCallback) {
        try {
            String currentBranch = git.getRepository().getBranch();
            GitCredentialUtils.addSSHRemote(git);
            git.push()
                    .setRemote(SSH_REMOTE)
                    .setRefSpecs(new RefSpec(currentBranch + ":" + currentBranch))
                    .setTransportConfigCallback(sshCallback)
                    .call();
            info("Pushed local branch '" + currentBranch + "' to origin.");
        } catch (IOException | GitAPIException | URISyntaxException e) {
            error("Failed to push branch", e);
            throw new RuntimeException("Failed to push branch", e);
        }
        return this;
    }

    /**
     * Creates a local Git tag with the given name and an autogenerated message.
     * This does not push the tag to the remote repository.
     *
     * @param tag the name of the tag to create
     * @return this GitCommand instance
     * @throws RuntimeException if the tag creation fails due to a Git error
     */
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

    /**
     * Pushes the specified tag to the origin remote using the appropriate authentication strategy.
     *
     * @param tag           the name of the tag to push
     * @param configuration the Git configuration containing authentication and server details
     * @return this GitCommand instance
     * @throws RuntimeException if the push operation fails
     */
    
    public GitCommand pushTag(String tag, GitConfiguration configuration) {
        if (GitCredentialUtils.isSSH(configuration.getScm())) {
            info("Using SSH to push tag: " + tag);
            return pushTag(tag, transport -> {
                if (transport instanceof SshTransport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                }
            });
        } else {
            info("Using HTTPS credentials to push tag: " + tag);
            String password = configuration.getSettings()
                    .getServer(configuration.getServerKey())
                    .getPassword();
            CredentialsProvider credentialsProvider = GitCredentialUtils.getUserProvider(password);
            return pushTag(tag, credentialsProvider);
        }
    }

    /**
     * Pushes the specified tag to the origin remote using HTTPS authentication.
     *
     * @param tag the name of the tag to push
     * @param credentialsProvider the credentials provider for remote access
     * @return this GitCommand instance
     * @throws RuntimeException if the push operation fails
     */
    public GitCommand pushTag(String tag, CredentialsProvider credentialsProvider) {
        try {
            RefSpec tagRefSpec = new RefSpec("refs/tags/" + tag + ":refs/tags/" + tag);
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(tagRefSpec)
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            info("Tag '" + tag + "' pushed to origin.");
        } catch (GitAPIException e) {
            error("Failed to push tag: " + tag, e);
            throw new RuntimeException("Failed to push tag: " + tag, e);
        }
        return this;
    }

    /**
     * Pushes the specified tag to the origin remote using SSH authentication.
     *
     * @param tag the name of the tag to push
     * @param sshCallback the SSH transport configuration callback
     * @return this GitCommand instance
     * @throws RuntimeException if the push operation fails
     */
    public GitCommand pushTag(String tag, TransportConfigCallback sshCallback) {
        try {
            RefSpec tagRefSpec = new RefSpec("refs/tags/" + tag + ":refs/tags/" + tag);
            GitCredentialUtils.addSSHRemote(git);
            git.push()
                    .setRemote(SSH_REMOTE)
                    .setRefSpecs(tagRefSpec)
                    .setTransportConfigCallback(sshCallback)
                    .call();
            info("Tag '" + tag + "' pushed to origin.");
        } catch (GitAPIException | URISyntaxException e) {
            error("Failed to push tag: " + tag, e);
            throw new RuntimeException("Failed to push tag: " + tag, e);
        }
        return this;
    }

    /**
     * Removes the specified tag both locally and remotely using the appropriate authentication strategy.
     *
     * @param tag           the name of the tag to remove
     * @param configuration the Git configuration containing authentication and server details
     * @return this GitCommand instance
     * @throws RuntimeException if the tag removal fails
     */
    public GitCommand removeTag(String tag, GitConfiguration configuration) {
        try {
            List<String> deleted = git.tagDelete()
                    .setTags(tag)
                    .call();

            if (deleted.isEmpty()) {
                warn("Tag '" + tag + "' not found locally.");
            } else {
                info("Tag '" + tag + "' deleted locally.");
            }

            if (GitCredentialUtils.isSSH(configuration.getScm())) {
                info("Using SSH to delete tag '" + tag + "' from origin");
                return removeTag(tag,transport -> {
                    if (transport instanceof SshTransport) {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                    }
                });
            } else {
                info("Using HTTPS credentials to delete tag '" + tag + "' from origin");
                String password = configuration.getSettings()
                        .getServer(configuration.getServerKey())
                        .getPassword();
                CredentialsProvider credentialsProvider = GitCredentialUtils.getUserProvider(password);
                info("Tag '" + tag + "' deleted from origin.");
               return removeTag(tag,credentialsProvider);
            }


        } catch (GitAPIException e) {
            error("Failed to remove tag: " + tag, e);
            throw new RuntimeException("Failed to remove tag: " + tag, e);
        }
    }

    /**
     * Removes the specified tag both locally and remotely using HTTPS authentication.
     *
     * @param tag the name of the tag to remove
     * @param credentialsProvider the credentials provider for remote access
     * @return this GitCommand instance
     * @throws RuntimeException if the tag removal fails
     */
    public GitCommand removeTag(String tag, CredentialsProvider credentialsProvider) {
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
                    .setCredentialsProvider(credentialsProvider)
                    .call();

            info("Tag '" + tag + "' deleted from origin.");
        } catch (GitAPIException e) {
            error("Failed to remove tag: " + tag, e);
            throw new RuntimeException("Failed to remove tag: " + tag, e);
        }
        return this;
    }

    /**
     * Removes the specified tag both locally and remotely using SSH authentication.
     *
     * @param tag the name of the tag to remove
     * @param sshCallback the SSH transport configuration callback
     * @return this GitCommand instance
     * @throws RuntimeException if the tag removal fails
     */
    public GitCommand removeTag(String tag, TransportConfigCallback sshCallback) {
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

            GitCredentialUtils.addSSHRemote(git);

            git.push()
                    .setRemote(SSH_REMOTE)
                    .setRefSpecs(refSpec)
                    .setTransportConfigCallback(sshCallback)
                    .call();

            info("Tag '" + tag + "' deleted from origin.");
        } catch (GitAPIException | URISyntaxException e) {
            error("Failed to remove tag: " + tag, e);
            throw new RuntimeException("Failed to remove tag: " + tag, e);
        }
        return this;
    }

    /**
     * Checks whether a branch with the given name exists locally or remotely on origin,
     * performing a fetch operation using the appropriate authentication strategy.
     *
     * @param branchName    the name of the branch to check
     * @param configuration the Git configuration containing authentication and server details
     * @return {@code true} if the branch exists locally or on origin; {@code false} otherwise
     * @throws RuntimeException if an error occurs during the check
     */
    public boolean checkIfBranchExists(String branchName, GitConfiguration configuration) {
        if (GitCredentialUtils.isSSH(configuration.getScm())) {
            info("Using SSH to check existence of branch: " + branchName);
            return checkIfBranchExists(branchName,transport -> {
                if (transport instanceof SshTransport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                }
            });
        } else {
            info("Using HTTPS credentials to check existence of branch: " + branchName);
            String password = configuration.getSettings()
                    .getServer(configuration.getServerKey())
                    .getPassword();
            CredentialsProvider credentialsProvider = GitCredentialUtils.getUserProvider(password);

            return checkIfBranchExists(branchName,credentialsProvider);
        }

    }

    /**
     * Checks whether a branch with the given name exists locally or remotely on origin,
     * performing a fetch operation using HTTPS authentication before checking.
     *
     * @param branchName the name of the branch to check
     * @param credentialsProvider the credentials provider for remote access
     * @return {@code true} if the branch exists locally or on origin; {@code false} otherwise
     * @throws RuntimeException if an error occurs during the check
     */
    public boolean checkIfBranchExists(String branchName, CredentialsProvider credentialsProvider) {
        try {
            git.fetch()
                    .setRemote("origin")
                    .setCredentialsProvider(credentialsProvider)
                    .call();

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

    /**
     * Checks whether a branch with the given name exists locally or remotely on origin,
     * performing a fetch operation using SSH authentication before checking.
     *
     * @param branchName the name of the branch to check
     * @param sshCallback the SSH transport configuration callback
     * @return {@code true} if the branch exists locally or on origin; {@code false} otherwise
     * @throws RuntimeException if an error occurs during the check
     */
    public boolean checkIfBranchExists(String branchName, TransportConfigCallback sshCallback) {
        try {
            GitCredentialUtils.addSSHRemote(git);
            git.fetch()
                    .setRemote(SSH_REMOTE)
                    .setTransportConfigCallback(sshCallback)
                    .call();

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
        } catch (GitAPIException | URISyntaxException e) {
            error("Failed to check branch existence: " + branchName, e);
            throw new RuntimeException("Failed to check branch existence: " + branchName, e);
        }
    }

    /**
     * Checks whether a Git tag exists locally or on the origin remote using the appropriate authentication strategy.
     *
     * @param tagName       the name of the tag to check
     * @param configuration the Git configuration containing authentication and server details
     * @return {@code true} if the tag exists locally or remotely; {@code false} otherwise
     * @throws RuntimeException if the check operation fails
     */
    public boolean checkIfTagExists(String tagName, GitConfiguration configuration) {
        if (GitCredentialUtils.isSSH(configuration.getScm())) {
            info("Using SSH to check existence of tag: " + tagName);
            return checkIfTagExists(tagName,transport -> {
                if (transport instanceof SshTransport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                }
            });
        } else {
            info("Using HTTPS credentials to check existence of tag: " + tagName);
            String password = configuration.getSettings()
                    .getServer(configuration.getServerKey())
                    .getPassword();
            CredentialsProvider credentialsProvider = GitCredentialUtils.getUserProvider(password);

            return checkIfTagExists(tagName,credentialsProvider);
        }
    }

    /**
     * Checks whether a Git tag exists locally or on the origin remote,
     * using HTTPS authentication.
     *
     * @param tagName the name of the tag to check
     * @param credentialsProvider the credentials provider for remote access
     * @return {@code true} if the tag exists locally or remotely; {@code false} otherwise
     * @throws RuntimeException if the check operation fails
     */
    public boolean checkIfTagExists(String tagName, CredentialsProvider credentialsProvider) {
        try {
            boolean localExists = git.tagList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/tags/" + tagName));

            Collection<Ref> remoteTags = git.lsRemote()
                    .setRemote("origin")
                    .setTags(true)
                    .setCredentialsProvider(credentialsProvider)
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

    /**
     * Checks whether a Git tag exists locally or on the origin remote,
     * using SSH authentication.
     *
     * @param tagName the name of the tag to check
     * @param sshCallback the SSH transport configuration callback
     * @return {@code true} if the tag exists locally or remotely; {@code false} otherwise
     * @throws RuntimeException if the check operation fails
     */
    public boolean checkIfTagExists(String tagName, TransportConfigCallback sshCallback) {
        try {
            boolean localExists = git.tagList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/tags/" + tagName));

            GitCredentialUtils.addSSHRemote(git);

            Collection<Ref> remoteTags = git.lsRemote()
                    .setRemote(SSH_REMOTE)
                    .setTags(true)
                    .setTransportConfigCallback(sshCallback)
                    .call();

            boolean remoteExists = remoteTags.stream()
                    .anyMatch(ref -> ref.getName().equals("refs/tags/" + tagName));

            if (localExists) info("Tag '" + tagName + "' exists locally.");
            if (remoteExists) info("Tag '" + tagName + "' exists on origin.");
            if (!localExists && !remoteExists) warn("Tag '" + tagName + "' does not exist locally or remotely.");

            return localExists || remoteExists;
        } catch (GitAPIException | URISyntaxException e) {
            error("Failed to check tag existence: " + tagName, e);
            throw new RuntimeException("Failed to check tag existence: " + tagName, e);
        }
    }

    /**
     * Stages all modified, new, and deleted files in the working directory for the next commit.
     * Equivalent to running {@code git add .} in the command line.
     *
     * @return this GitCommand instance
     * @throws RuntimeException if staging the changes fails due to a Git API error
     */
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

    /**
     * Stages files matching the provided file pattern for the next commit.
     * If the pattern is empty or blank, defaults to {@code "."}, equivalent to staging all changes.
     *
     * @param filePattern an optional file pattern to stage (e.g., "src/", "*.java", etc.)
     * @return this GitCommand instance
     * @throws RuntimeException if the staging operation fails due to a Git API error
     */
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

    /**
     * Commits all staged changes with the provided commit message.
     * This method assumes that files have already been added to the index.
     *
     * @param message the commit message to associate with the commit
     * @return this GitCommand instance
     * @throws RuntimeException if the commit operation fails due to a Git API error
     */
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

    /**
     * Resets the current working directory and index to the last commit using a hard reset.
     * This discards all uncommitted changes and staged files, effectively reverting the working state
     * to match the latest HEAD commit. Equivalent to running {@code git reset --hard}.
     *
     * @return this GitCommand instance
     * @throws RuntimeException if the reset operation fails due to a Git API error
     */
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

    /**
     * Performs a hard reset on the specified file pattern, discarding all changes
     * in the working directory and index for those files. If no pattern is provided
     * or it is blank, defaults to {@code "."}, which resets all files.
     *
     * @param filePattern an optional file pattern to reset (e.g., "src/", "*.java", etc.)
     * @return this GitCommand instance
     * @throws RuntimeException if the reset operation fails due to a Git API error
     */
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

    /**
     * Pulls changes from the origin remote using the appropriate authentication strategy.
     *
     * @param configuration the Git configuration containing authentication and server details
     * @return this GitCommand instance
     * @throws RuntimeException if the pull operation fails
     */
    public GitCommand pull(GitConfiguration configuration) {
        if (GitCredentialUtils.isSSH(configuration.getScm())) {
            info("Using SSH for pull");
            return pull(transport -> {
                if (transport instanceof SshTransport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                }
            });
        } else {
            info("Using HTTPS credentials for pull");
            String password = configuration.getSettings()
                    .getServer(configuration.getServerKey())
                    .getPassword();
            CredentialsProvider credentialsProvider =
                    GitCredentialUtils.getUserProvider(password);
            return pull(credentialsProvider);
        }
    }

    /**
     * Pulls changes from the origin remote using HTTPS authentication.
     *
     * @param credentialsProvider the credentials provider for remote access
     * @return this GitCommand instance
     * @throws RuntimeException if the pull operation fails
     */
    public GitCommand pull(CredentialsProvider credentialsProvider) {
        try (Git git = Git.open(new File("."))) {
            Repository repo = git.getRepository();
            ObjectId oldHead = repo.resolve("HEAD^{tree}");

            GitCredentialUtils.addSSHRemote(git);

            PullResult result = git.pull()
                    .setRemote("origin")
                    .setCredentialsProvider(credentialsProvider)
                    .call();

            MergeResult merge = result.getMergeResult();
            if (merge == null || !merge.getMergeStatus().isSuccessful()) {
                info("No merge occurred or merge was not successful.");
                return this;
            }

            ObjectId newHead = repo.resolve("HEAD^{tree}");
            if (Objects.equals(oldHead, newHead)) {
                info("Pull completed: repository already up to date. No changes.");
                return this;
            }

            try (ObjectReader reader = repo.newObjectReader()) {
                CanonicalTreeParser oldTree = new CanonicalTreeParser();
                CanonicalTreeParser newTree = new CanonicalTreeParser();
                oldTree.reset(reader, oldHead);
                newTree.reset(reader, newHead);

                List<DiffEntry> diffs = git.diff()
                        .setOldTree(oldTree)
                        .setNewTree(newTree)
                        .call();

                if (diffs.isEmpty()) {
                    log.info("Pull completed: no file-level changes detected.");
                } else {
                    for (DiffEntry diff : diffs) {
                        log.info(String.format("Changed: %s %s → %s", diff.getChangeType(), diff.getOldPath(), diff.getNewPath()));
                    }
                }
            }
        } catch (URISyntaxException | GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
        return this;

    }

    /**
     * Pulls changes from the origin remote using SSH authentication.
     *
     * @param sshCallback the SSH transport configuration callback
     * @return this GitCommand instance
     * @throws RuntimeException if the pull operation fails
     */
    public GitCommand pull(TransportConfigCallback sshCallback) {
        try (Git git = Git.open(new File("."))) {
            Repository repo = git.getRepository();
            ObjectId oldHead = repo.resolve("HEAD^{tree}");

            GitCredentialUtils.addSSHRemote(git);

            PullResult result = git.pull()
                    .setRemote(SSH_REMOTE)
                    .setTransportConfigCallback(sshCallback)
                    .call();

            MergeResult merge = result.getMergeResult();
            if (merge == null || !merge.getMergeStatus().isSuccessful()) {
                info("No merge occurred or merge was not successful.");
                return this;
            }

            ObjectId newHead = repo.resolve("HEAD^{tree}");
            if (Objects.equals(oldHead, newHead)) {
               info("Pull completed: repository already up to date. No changes.");
                return this;
            }

            try (ObjectReader reader = repo.newObjectReader()) {
                CanonicalTreeParser oldTree = new CanonicalTreeParser();
                CanonicalTreeParser newTree = new CanonicalTreeParser();
                oldTree.reset(reader, oldHead);
                newTree.reset(reader, newHead);

                List<DiffEntry> diffs = git.diff()
                        .setOldTree(oldTree)
                        .setNewTree(newTree)
                        .call();

                if (diffs.isEmpty()) {
                    log.info("Pull completed: no file-level changes detected.");
                } else {
                    for (DiffEntry diff : diffs) {
                        log.info(String.format("Changed: %s %s → %s", diff.getChangeType(), diff.getOldPath(), diff.getNewPath()));
                    }
                }
            }
        } catch (URISyntaxException | GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Pushes all changes from the current repository to the origin remote using the appropriate authentication strategy.
     *
     * @param configuration the Git configuration containing authentication and server details
     * @return this GitCommand instance
     * @throws RuntimeException if the push operation fails
     */
    public GitCommand push(GitConfiguration configuration) {
        info("PUSH");
//        if(!configuration.isPushChanges()){
//            info("Do not push");
//            return this;
//        }



        if (GitCredentialUtils.isSSH(configuration.getScm())) {
            return push(transport -> {
                if (transport instanceof SshTransport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                }
            });
        } else {
            info("Using HTTPS credentials for push");
            String password = configuration.getSettings()
                    .getServer(configuration.getServerKey())
                    .getPassword();
            CredentialsProvider credentialsProvider =
                    GitCredentialUtils.getUserProvider(password);
            return push(credentialsProvider);
        }
    }

    /**
     * Pushes all changes from the current repository to the origin remote using HTTPS authentication.
     *
     * @param credentialsProvider the credentials provider for remote access
     * @return this GitCommand instance
     * @throws RuntimeException if the push operation fails
     */
    public GitCommand push(CredentialsProvider credentialsProvider) {
        try (Git git = Git.open(new File("."))) {

            Iterable<PushResult> results = git.push()
                    .setRemote("origin")
                    .setCredentialsProvider(credentialsProvider)
                    .call();

            for (PushResult result : results) {
                for (RemoteRefUpdate update : result.getRemoteUpdates()) {
                    log.info(String.format("Update status: %s - %s → %s",
                            update.getStatus(),
                            update.getSrcRef(),
                            update.getRemoteName()));
                }
            }
            info("Push to origin completed successfully.");
        } catch (GitAPIException | IOException e) {
            error("Failed to push changes", e);
            throw new RuntimeException("Failed to push changes", e);
        }
        return this;
    }

    /**
     * Pushes all changes from the current repository to the origin remote using SSH authentication.
     *
     * @param sshCallback the SSH transport configuration callback
     * @return this GitCommand instance
     * @throws RuntimeException if the push operation fails
     */
    public GitCommand push(TransportConfigCallback sshCallback) {
        try (Git git = Git.open(new File("."))) {
            GitCredentialUtils.addSSHRemote(git);

            Iterable<PushResult> results = git.push()
                    .setRemote(SSH_REMOTE)
                    .setTransportConfigCallback(sshCallback)
                    .call();

            for (PushResult result : results) {
                for (RemoteRefUpdate update : result.getRemoteUpdates()) {
                    log.info(String.format("Update status: %s - %s → %s",
                            update.getStatus(),
                            update.getSrcRef(),
                            update.getRemoteName()));
                }
            }
            info("Push to SSH remote completed successfully.");
        } catch (GitAPIException | IOException | URISyntaxException e) {
            error("Failed to push changes via SSH", e);
            throw new RuntimeException("Failed to push changes", e);
        }
        return this;
    }

    /**
     * Fetches changes from the origin remote using the appropriate authentication strategy.
     *
     * @param configuration the Git configuration containing authentication and server details
     * @return this GitCommand instance
     * @throws RuntimeException if the fetch operation fails
     */
    public GitCommand fetch(GitConfiguration configuration) {
        if (GitCredentialUtils.isSSH(configuration.getScm())) {
            info("Using SSH for fetch");
            return fetch(transport -> {
                if (transport instanceof SshTransport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                }
            });
        } else {
            info("Using HTTPS credentials for fetch");
            String password = configuration.getSettings()
                    .getServer(configuration.getServerKey())
                    .getPassword();
            CredentialsProvider credentialsProvider =
                    GitCredentialUtils.getUserProvider(password);
            return fetch(credentialsProvider);
        }
    }

    /**
     * Fetches changes from the origin remote using HTTPS authentication.
     *
     * @param credentialsProvider the credentials provider for remote access
     * @return this GitCommand instance
     * @throws RuntimeException if the fetch operation fails
     */
    public GitCommand fetch(CredentialsProvider credentialsProvider) {
        try {
            git.fetch()
                    .setRemote("origin")
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            info("Fetched changes from origin.");
        } catch (GitAPIException e) {
            error("Failed to fetch changes", e);
            throw new RuntimeException("Failed to fetch changes", e);
        }
        return this;
    }

    /**
     * Fetches changes from the origin remote using SSH authentication.
     *
     * @param sshCallback the SSH transport configuration callback
     * @return this GitCommand instance
     * @throws RuntimeException if the fetch operation fails
     */
    public GitCommand fetch(TransportConfigCallback sshCallback) {
        try {
            GitCredentialUtils.addSSHRemote(git);
            git.fetch()
                    .setRemote(SSH_REMOTE)
                    .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/origin/*"))
                    .setTransportConfigCallback(sshCallback)
                    .call();
            info("Fetched changes from origin.");
        } catch (GitAPIException | URISyntaxException e) {
            error("Failed to fetch changes", e);
            throw new RuntimeException("Failed to fetch changes", e);
        }
        return this;
    }

    /**
     * Deletes the specified local Git branch using force deletion.
     * This operation does not affect remote branches.
     *
     * @param branchName the name of the local branch to delete
     * @return this GitCommand instance
     * @throws RuntimeException if the branch deletion fails due to a Git API error
     */
    public GitCommand deleteLocalBranch(String branchName) {
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

    /**
     * Deletes the specified branch from the origin remote using the appropriate authentication strategy.
     *
     * @param branchName     the name of the branch to delete remotely
     * @param configuration  the Git configuration containing authentication and server details
     * @return this GitCommand instance
     * @throws RuntimeException if the branch deletion fails
     */
    public GitCommand deleteRemoteBranch(String branchName, GitConfiguration configuration) {
        if (GitCredentialUtils.isSSH(configuration.getScm())) {
            info("Using SSH to delete remote branch: " + branchName);
            return deleteRemoteBranch(branchName, transport -> {
                if (transport instanceof SshTransport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                }
            });
        } else {
            info("Using HTTPS credentials to delete remote branch: " + branchName);
            String password = configuration.getSettings()
                    .getServer(configuration.getServerKey())
                    .getPassword();
            CredentialsProvider credentialsProvider = GitCredentialUtils.getUserProvider(password);
            return deleteRemoteBranch(branchName, credentialsProvider);
        }
    }

    /**
     * Deletes the specified branch from the origin remote using HTTPS authentication.
     *
     * @param branchName the name of the branch to delete remotely
     * @param credentialsProvider the credentials provider for remote access
     * @return this GitCommand instance
     * @throws RuntimeException if the branch deletion fails
     */
    public GitCommand deleteRemoteBranch(String branchName, CredentialsProvider credentialsProvider) {
        try {
            RefSpec refSpec = new RefSpec(":" + branchName);
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(refSpec)
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            info("Deleted remote branch: " + branchName);
        } catch (GitAPIException e) {
            error("Failed to delete remote branch: " + branchName, e);
            throw new RuntimeException("Failed to delete remote branch", e);
        }
        return this;
    }

    /**
     * Deletes the specified branch from the origin remote using SSH authentication.
     *
     * @param branchName the name of the branch to delete remotely
     * @param sshCallback the SSH transport configuration callback
     * @return this GitCommand instance
     * @throws RuntimeException if the branch deletion fails
     */
    public GitCommand deleteRemoteBranch(String branchName, TransportConfigCallback sshCallback) {
        try {
            RefSpec refSpec = new RefSpec(":" + branchName);
            GitCredentialUtils.addSSHRemote(git);
            git.push()
                    .setRemote(SSH_REMOTE)
                    .setRefSpecs(refSpec)
                    .setTransportConfigCallback(sshCallback)
                    .call();
            info("Deleted remote branch: " + branchName);
        } catch (GitAPIException | URISyntaxException e) {
            error("Failed to delete remote branch: " + branchName, e);
            throw new RuntimeException("Failed to delete remote branch", e);
        }
        return this;
    }

    /**
     * Deletes the specified Git tag from the local repository.
     * This operation does not affect tags stored on remote repositories.
     *
     * @param tagName the name of the tag to delete locally
     * @return this GitCommand instance
     * @throws RuntimeException if the tag deletion fails due to a Git API error
     */
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

    /**
     * Deletes the specified tag from the origin remote using the appropriate authentication strategy.
     *
     * @param tagName       the name of the tag to delete remotely
     * @param configuration the Git configuration containing authentication and server details
     * @return this GitCommand instance
     * @throws RuntimeException if the tag deletion fails
     */
    public GitCommand deleteRemoteTag(String tagName, GitConfiguration configuration) {
        if (GitCredentialUtils.isSSH(configuration.getScm())) {
            info("Using SSH to delete remote tag: " + tagName);
            return deleteRemoteTag(tagName,transport -> {
                if (transport instanceof SshTransport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                }
            });
        } else {
            info("Using HTTPS credentials to delete remote tag: " + tagName);
            String password = configuration.getSettings()
                    .getServer(configuration.getServerKey())
                    .getPassword();
            CredentialsProvider credentialsProvider = GitCredentialUtils.getUserProvider(password);
            return deleteRemoteTag(tagName, credentialsProvider);
        }
    }

    /**
     * Deletes the specified tag from the origin remote using HTTPS authentication.
     *
     * @param tagName the name of the tag to delete remotely
     * @param credentialsProvider the credentials provider for remote access
     * @return this GitCommand instance
     * @throws RuntimeException if the tag deletion fails
     */
    public GitCommand deleteRemoteTag(String tagName, CredentialsProvider credentialsProvider) {
        try {
            RefSpec refSpec = new RefSpec("refs/tags/" + tagName + ":");
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(refSpec)
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            info("Deleted remote tag: " + tagName);
        } catch (GitAPIException e) {
            error("Failed to delete remote tag: " + tagName, e);
            throw new RuntimeException("Failed to delete remote tag", e);
        }
        return this;
    }

    /**
     * Deletes the specified tag from the origin remote using SSH authentication.
     *
     * @param tagName the name of the tag to delete remotely
     * @param sshCallback the SSH transport configuration callback
     * @return this GitCommand instance
     * @throws RuntimeException if the tag deletion fails
     */
    public GitCommand deleteRemoteTag(String tagName, TransportConfigCallback sshCallback) {
        try {
            RefSpec refSpec = new RefSpec("refs/tags/" + tagName + ":");
            GitCredentialUtils.addSSHRemote(git);
            git.push()
                    .setRemote(SSH_REMOTE)
                    .setRefSpecs(refSpec)
                    .setTransportConfigCallback(sshCallback)
                    .call();
            info("Deleted remote tag: " + tagName);
        } catch (GitAPIException | URISyntaxException e) {
            error("Failed to delete remote tag: " + tagName, e);
            throw new RuntimeException("Failed to delete remote tag", e);
        }
        return this;
    }

    /**
     * Closes the underlying Git repository, releasing any held resources.
     * After calling this method, no further Git operations should be performed
     * with this instance.
     *
     * @return this GitCommand instance
     */
    public GitCommand close() {
        git.close();
        info("Closed git repository.");
        return this;
    }

    /**
     * Executes the provided {@link PomCommand} using the specified {@link Consumer}.
     * This is a flexible way to apply operations or transformations on a Maven POM file.
     *
     * @param pomCommandConsumer the consumer that defines how the command should be executed
     * @param command the PomCommand instance to execute
     * @return this GitCommand instance
     */
    public GitCommand runPomCommands(@NotNull Consumer<PomCommand> pomCommandConsumer, PomCommand command) {
        pomCommandConsumer.accept(command);
        return this;
    }

    /**
     * Executes the provided {@link ShellCommand} using the specified {@link Consumer}.
     * Run a shell command.
     *
     * @param shellComandConsumer the consumer that defines how the command should be executed
     * @param command the ShellCommand instance to execute
     * @return this GitCommand instance
     */
    public GitCommand runShellCommands(@NotNull Consumer<ShellCommand> shellComandConsumer, ShellCommand command) {
        shellComandConsumer.accept(command);
        return this;
    }

    public GitCommand mergeBranches(@NotNull String from, @NotNull String to, GitConfiguration configuration) {
        try {
            // Determine fetch strategy
            if (GitCredentialUtils.isSSH(configuration.getScm())) {
                info("Using SSH for branch fetch before merge");
                fetch(transport -> {
                    if (transport instanceof SshTransport) {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                    }
                });
            } else {
                info("Using HTTPS credentials for branch fetch before merge");
                String password = configuration.getSettings()
                        .getServer(configuration.getServerKey())
                        .getPassword();
                CredentialsProvider credentialsProvider = GitCredentialUtils.getUserProvider(password);
                fetch(credentialsProvider);
            }

            // Checkout target branch
            git.checkout()
                    .setCreateBranch(true)
                    .setName(to)
                    .setStartPoint("origin/" + to)
                    .call();

            info("Checked out target branch: " + to);

            // Merge source branch into target
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
                    error("Merge failed with status: " + result.getMergeStatus(), null);
                    throw new RuntimeException("Merge failed: " + result.getMergeStatus());
            }

        } catch (GitAPIException | IOException e) {
            error("Error merging " + from + " into " + to, e);
            throw new RuntimeException("Failed to merge branches", e);
        }

        return this;
    }

    /**
     * Merges the specified source branch into the target branch.
     * Automatically checks out the target branch before attempting the merge.
     * Supported merge outcomes include fast-forward, regular merge, and squash merge.
     * Any other merge status will result in a failure and throw an exception.
     *
     * @param from the name of the source branch to merge from
     * @param to the name of the target branch to merge into (will be checked out)
     * @return this GitCommand instance
     * @throws RuntimeException if the checkout or merge operation fails
     */
    public GitCommand mergeBranches(@NotNull String from, @NotNull String to) {
        try {

            // Checkout target branch (to)
            git.checkout().setName(to).setStartPoint("origin/" + to).call();
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
                    error("Merge failed with status: " + result.getMergeStatus(), null);
                    throw new RuntimeException("Merge failed: " + result.getMergeStatus());
            }
        } catch (GitAPIException | IOException e) {
            error("Error merging " + from + " into " + to, e);
            throw new RuntimeException("Failed to merge branches", e);
        }

        return this;
    }

    /**
     * Performs a merge from the specified source branch into the target branch,
     * using {@code --no-ff} (no fast-forward) mode and without committing the result.
     * This allows for further inspection or modification before a commit.
     * <p>
     * The {@code exclusion} parameter is accepted but not currently applied within the logic.
     * You may enhance this method later to exclude specific files or paths during the merge.
     *
     * @param from the name of the source branch to merge from
     * @param to the name of the target branch to merge into (this branch will be checked out)
     * @param exclusion a placeholder for an exclusion rule, currently unused
     * @return this GitCommand instance
     * @throws RuntimeException if the checkout or merge operation fails
     */
    public GitCommand mergeBranchesWithExclusion(@NotNull String from, @NotNull String to, String exclusion) {
        try {
            // Checkout target branch (to)
            git.checkout().setName(to).call();
            info("Checked out target branch: " + to);

            // Merge source branch (from) into target with no fast-forward and no commit
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
                    error("Merge failed with status: " + result.getMergeStatus(), null);
                    throw new RuntimeException("Merge failed: " + result.getMergeStatus());
            }

        } catch (GitAPIException | IOException e) {
            error("Error merging " + from + " into " + to, e);
            throw new RuntimeException("Failed to merge branches", e);
        }

        return this;
    }

    public GitCommand when(Consumer<GitCommand> gitCommandConsumer){
        gitCommandConsumer.accept(this);
        return this;
    }

}