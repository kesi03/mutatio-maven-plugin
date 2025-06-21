package com.mockholm.utils;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
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

public class GitUtils {
    public static String getCurrentBranch(){
        String branch="";
        Git git = null;
        try {
            git = Git.open(new File("."));
            branch = git.getRepository().getBranch();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return branch;
    }

    public static void changeBranch(String targetBranch) {
        Git git = null;
        try {
            git = Git.open(new File("."));
            String currentBranch = git.getRepository().getBranch();

            if (Objects.equals(currentBranch, targetBranch)) {
                System.out.println("Already on branch '" + targetBranch + "'.");
            }

            boolean localExists = git.branchList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + targetBranch));

            if (localExists) {
                git.checkout().setName(targetBranch).call();
                System.out.println("Switched to existing local branch '" + targetBranch + "'.");
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
                System.out.println("Created and switched to branch '" + targetBranch + "' tracking origin.");
            } else {
                git.checkout()
                        .setCreateBranch(true)
                        .setName(targetBranch)
                        .call();
                System.out.println("Created and switched to new local branch '" + targetBranch + "'.");
            }
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to change branch to '" + targetBranch + "'"+e.getMessage());
            throw new RuntimeException("Failed to change branch", e);
        }
    }

    public static void changeBranch(String targetBranch, Log log) {
        try (Git git = Git.open(new File("."))) {
            String currentBranch = git.getRepository().getBranch();

            if (Objects.equals(currentBranch, targetBranch)) {
                log.info("Already on branch '" + targetBranch + "'.");
                return;
            }

            boolean localExists = git.branchList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + targetBranch));

            if (localExists) {
                git.checkout().setName(targetBranch).call();
                log.info("Switched to existing local branch '" + targetBranch + "'.");
                return;
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
                log.info("Created and switched to branch '" + targetBranch + "' tracking origin.");
            } else {
                git.checkout()
                        .setCreateBranch(true)
                        .setName(targetBranch)
                        .call();
                log.info("Created and switched to new local branch '" + targetBranch + "'.");
            }

        } catch (IOException | GitAPIException e) {
            log.error("Failed to change branch to '" + targetBranch + "'", e);
            throw new RuntimeException("Failed to change branch", e);
        }
    }

    public static void createBranch(String branchName) {
        try (Git git = Git.open(new File("."))) {

            // Check if branch already exists locally
            boolean localExists = git.branchList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + branchName));

            if (localExists) {
                System.out.println("Branch '" + branchName + "' already exists locally.");
                return;
            }

            // Check if branch exists remotely
            boolean remoteExists = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/remotes/origin/" + branchName));

            if (remoteExists) {
                // Create local branch tracking the remote one
                git.checkout()
                        .setCreateBranch(true)
                        .setName(branchName)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .setStartPoint("origin/" + branchName)
                        .call();
                System.out.println("Created and checked out branch '" + branchName + "' tracking origin.");
            } else {
                // Create a new local branch from current HEAD
                git.checkout()
                        .setCreateBranch(true)
                        .setName(branchName)
                        .call();
                System.out.println("Created new local branch '" + branchName + "'.");
            }

        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Failed to create branch: " + branchName, e);
        }
    }

    public static void createBranch(String branchName, Log log) {
        try (Git git = Git.open(new File("."))) {
            boolean localExists = git.branchList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + branchName));

            if (localExists) {
                log.info("Branch '" + branchName + "' already exists locally.");
                return;
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
                log.info("Created and checked out branch '" + branchName + "' tracking origin.");
            } else {
                git.checkout()
                        .setCreateBranch(true)
                        .setName(branchName)
                        .call();
                log.info("Created new local branch '" + branchName + "'.");
            }

        } catch (IOException | GitAPIException e) {
            log.error("Failed to create branch: " + branchName, e);
            throw new RuntimeException("Failed to create branch: " + branchName, e);
        }
    }

    public static void pushBranch() {
        try (Git git = Git.open(new File("."))) {
            String currentBranch = git.getRepository().getBranch();

            // Push the local branch to origin, creating it if it doesn't exist remotely
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(new RefSpec(currentBranch + ":" + currentBranch))
                    .call();

            System.out.println("Pushed local branch '" + currentBranch + "' to origin.");
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Failed to push branch", e);
        }
    }

    public static void pushBranch(@NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            String currentBranch = git.getRepository().getBranch();

            git.push()
                    .setRemote("origin")
                    .setRefSpecs(new RefSpec(currentBranch + ":" + currentBranch))
                    .call();

            log.info("Pushed local branch '" + currentBranch + "' to origin.");
        } catch (IOException | GitAPIException e) {
            log.error("Failed to push branch to origin", e);
            throw new RuntimeException("Failed to push branch", e);
        }
    }

    public static void createTag(String tag) {
        try (Git git = Git.open(new File("."))) {
            git.tag()
                    .setName(tag)
                    .setMessage("Created tag: " + tag)
                    .call();

            System.out.println("Tag '" + tag + "' created locally.");
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Failed to create tag: " + tag, e);
        }
    }

    public static void createTag(String tag, @NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            git.tag()
                    .setName(tag)
                    .setMessage("Created tag: " + tag)
                    .call();

            log.info("Tag '" + tag + "' created locally.");
        } catch (IOException | GitAPIException e) {
            log.error("Failed to create tag: " + tag, e);
            throw new RuntimeException("Failed to create tag: " + tag, e);
        }
    }

    public static void pushTag(String tag) {
        try (Git git = Git.open(new File("."))) {
            RefSpec tagRefSpec = new RefSpec("refs/tags/" + tag + ":refs/tags/" + tag);

            git.push()
                    .setRemote("origin")
                    .setRefSpecs(tagRefSpec)
                    .call();

            System.out.println("Tag '" + tag + "' pushed to origin.");
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Failed to push tag: " + tag, e);
        }
    }

    public static void pushTag(String tag, @NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            RefSpec tagRefSpec = new RefSpec("refs/tags/" + tag + ":refs/tags/" + tag);

            git.push()
                    .setRemote("origin")
                    .setRefSpecs(tagRefSpec)
                    .call();

            log.info("Tag '" + tag + "' pushed to origin.");
        } catch (IOException | GitAPIException e) {
            log.error("Failed to push tag: " + tag, e);
            throw new RuntimeException("Failed to push tag: " + tag, e);
        }
    }

    public static void removeTag(String tag) {
        try (Git git = Git.open(new File("."))) {
            // Delete the local tag
            List<String> deleted = git.tagDelete()
                    .setTags(tag)
                    .call();

            if (deleted.isEmpty()) {
                System.out.println("Tag '" + tag + "' not found locally.");
            } else {
                System.out.println("Tag '" + tag + "' deleted locally.");
            }

            // Delete the tag from the remote
            RefSpec refSpec = new RefSpec()
                    .setSource(null) // null source means delete
                    .setDestination("refs/tags/" + tag);

            git.push()
                    .setRemote("origin")
                    .setRefSpecs(refSpec)
                    .call();

            System.out.println("Tag '" + tag + "' deleted from origin.");

        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Failed to remove tag: " + tag, e);
        }
    }

    public static void removeTag(String tag, Log log) {
        try (Git git = Git.open(new File("."))) {
            List<String> deleted = git.tagDelete()
                    .setTags(tag)
                    .call();

            if (deleted.isEmpty()) {
                log.warn("Tag '" + tag + "' not found locally.");
            } else {
                log.info("Tag '" + tag + "' deleted locally.");
            }

            RefSpec refSpec = new RefSpec()
                    .setSource(null)
                    .setDestination("refs/tags/" + tag);

            git.push()
                    .setRemote("origin")
                    .setRefSpecs(refSpec)
                    .call();

            log.info("Tag '" + tag + "' deleted from origin.");

        } catch (IOException | GitAPIException e) {
            log.error("Failed to remove tag: " + tag, e);
            throw new RuntimeException("Failed to remove tag: " + tag, e);
        }
    }

    public static boolean checkIfBranchExists(String branchName) {
        try (Git git = Git.open(new File("."))) {
            // Check local branches
            boolean localExists = git.branchList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + branchName));

            // Check remote branches
            boolean remoteExists = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/remotes/origin/" + branchName));

            return localExists || remoteExists;

        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Failed to check branch existence: " + branchName, e);
        }
    }

    public static boolean checkIfBranchExists(String branchName, Log log) {
        try (Git git = Git.open(new File("."))) {
            boolean localExists = git.branchList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + branchName));

            boolean remoteExists = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/remotes/origin/" + branchName));

            if (localExists) {
                log.info("Branch '" + branchName + "' exists locally.");
            }
            if (remoteExists) {
                log.info("Branch '" + branchName + "' exists on origin.");
            }
            if (!localExists && !remoteExists) {
                log.warn("Branch '" + branchName + "' does not exist locally or remotely.");
            }

            return localExists || remoteExists;

        } catch (IOException | GitAPIException e) {
            log.error("Failed to check branch existence: " + branchName, e);
            throw new RuntimeException("Failed to check branch existence: " + branchName, e);
        }
    }

    public static boolean checkIfTagExists(String tagName) {
        try (Git git = Git.open(new File("."))) {
            // Check local tags
            boolean localExists = git.tagList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/tags/" + tagName));

            // Check remote tags
            Collection<Ref> remoteTags = git.lsRemote()
                    .setTags(true)
                    .setRemote("origin")
                    .call();

            boolean remoteExists = remoteTags.stream()
                    .anyMatch(ref -> ref.getName().equals("refs/tags/" + tagName));

            return localExists || remoteExists;

        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Failed to check tag existence: " + tagName, e);
        }
    }

    public static boolean checkIfTagExists(String tagName, Log log) {
        try (Git git = Git.open(new File("."))) {
            // Check local tags
            boolean localExists = git.tagList()
                    .call()
                    .stream()
                    .anyMatch(ref -> ref.getName().equals("refs/tags/" + tagName));

            // Check remote tags
            Collection<Ref> remoteTags = git.lsRemote()
                    .setTags(true)
                    .setRemote("origin")
                    .call();

            boolean remoteExists = remoteTags.stream()
                    .anyMatch(ref -> ref.getName().equals("refs/tags/" + tagName));

            if (localExists) {
                log.info("Tag '" + tagName + "' exists locally.");
            }
            if (remoteExists) {
                log.info("Tag '" + tagName + "' exists on origin.");
            }
            if (!localExists && !remoteExists) {
                log.warn("Tag '" + tagName + "' does not exist locally or remotely.");
            }

            return localExists || remoteExists;

        } catch (IOException | GitAPIException e) {
            log.error("Failed to check tag existence: " + tagName, e);
            throw new RuntimeException("Failed to check tag existence: " + tagName, e);
        }
    }

    public static void addAllChanges() {
        try (Git git = Git.open(new File("."))) {
            git.add()
                    .addFilepattern(".")
                    .call();

            System.out.println("Staged all changes (equivalent to 'git add .').");
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to stage changes: " + e.getMessage());
            throw new RuntimeException("Failed to stage changes", e);
        }
    }

    public static void addAllChanges(@NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            git.add()
                    .addFilepattern(".")
                    .call();

            log.info("Staged all changes (equivalent to 'git add .').");
        } catch (IOException | GitAPIException e) {
            log.error("Failed to stage changes", e);
            throw new RuntimeException("Failed to stage changes", e);
        }
    }

    public static void add(@NotNull Optional<String> filePattern) {
        try (Git git = Git.open(new File("."))) {
            String pattern = filePattern.filter(p -> !p.isBlank()).orElse(".");
            git.add()
                    .addFilepattern(pattern)
                    .call();

            System.out.println("Staged changes for pattern: " + pattern);
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to stage changes: " + e.getMessage());
            throw new RuntimeException("Failed to stage changes", e);
        }
    }

    public static void add(@NotNull Optional<String> filePattern, @NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            String pattern = filePattern.filter(p -> !p.isBlank()).orElse(".");
            git.add()
                    .addFilepattern(pattern)
                    .call();

            log.info("Staged changes for pattern: " + pattern);
        } catch (IOException | GitAPIException e) {
            log.error("Failed to stage changes for pattern: " + filePattern.orElse("."), e);
            throw new RuntimeException("Failed to stage changes", e);
        }
    }

    public static void commit(String message) {
        try (Git git = Git.open(new File("."))) {
            git.commit()
                    .setMessage(message)
                    .call();

            System.out.println("Committed changes with message: " + message);
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to commit changes: " + e.getMessage());
            throw new RuntimeException("Failed to commit changes", e);
        }
    }

    public static void commit(String message, @NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            git.commit()
                    .setMessage(message)
                    .call();

            log.info("Committed changes with message: " + message);
        } catch (IOException | GitAPIException e) {
            log.error("Failed to commit changes", e);
            throw new RuntimeException("Failed to commit changes", e);
        }
    }

    public static void reset() {
        try (Git git = Git.open(new File("."))) {
            git.reset()
                    .setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD)
                    .call();

            System.out.println("Reset all changes (equivalent to 'git reset --hard').");
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to reset changes: " + e.getMessage());
            throw new RuntimeException("Failed to reset changes", e);
        }
    }

    public static void reset(@NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            git.reset()
                    .setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD)
                    .call();

            log.info("Reset all changes (equivalent to 'git reset --hard').");
        } catch (IOException | GitAPIException e) {
            log.error("Failed to reset changes", e);
            throw new RuntimeException("Failed to reset changes", e);
        }
    }

    public static void reset(@NotNull Optional<String> filePattern) {
        try (Git git = Git.open(new File("."))) {
            String pattern = filePattern.filter(p -> !p.isBlank()).orElse(".");
            git.reset()
                    .setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD)
                    .addPath(pattern)
                    .call();

            System.out.println("Reset changes for pattern: " + pattern);
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to reset changes: " + e.getMessage());
            throw new RuntimeException("Failed to reset changes", e);
        }
    }

    public static void reset(@NotNull Optional<String> filePattern, @NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            String pattern = filePattern.filter(p -> !p.isBlank()).orElse(".");
            git.reset()
                    .setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD)
                    .addPath(pattern)
                    .call();

            log.info("Reset changes for pattern: " + pattern);
        } catch (IOException | GitAPIException e) {
            log.error("Failed to reset changes for pattern: " + filePattern.orElse("."), e);
            throw new RuntimeException("Failed to reset changes", e);
        }
    }

    public static void pull() {
        try (Git git = Git.open(new File("."))) {
            git.pull()
                    .setRemote("origin")
                    .call();

            System.out.println("Pulled changes from origin.");
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to pull changes: " + e.getMessage());
            throw new RuntimeException("Failed to pull changes", e);
        }
    }

    public static void pull(@NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            git.pull()
                    .setRemote("origin")
                    .call();

            log.info("Pulled changes from origin.");
        } catch (IOException | GitAPIException e) {
            log.error("Failed to pull changes", e);
            throw new RuntimeException("Failed to pull changes", e);
        }
    }

    public static void push() {
        try (Git git = Git.open(new File("."))) {
            git.push()
                    .setRemote("origin")
                    .call();

            System.out.println("Pushed changes to origin.");
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to push changes: " + e.getMessage());
            throw new RuntimeException("Failed to push changes", e);
        }
    }

    public static void push(@NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            git.push()
                    .setRemote("origin")
                    .call();

            log.info("Pushed changes to origin.");
        } catch (IOException | GitAPIException e) {
            log.error("Failed to push changes", e);
            throw new RuntimeException("Failed to push changes", e);
        }
    }

    public static void fetch() {
        try (Git git = Git.open(new File("."))) {
            git.fetch()
                    .setRemote("origin")
                    .call();

            System.out.println("Fetched changes from origin.");
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to fetch changes: " + e.getMessage());
            throw new RuntimeException("Failed to fetch changes", e);
        }
    }

    public static void fetch(@NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            git.fetch()
                    .setRemote("origin")
                    .call();

            log.info("Fetched changes from origin.");
        } catch (IOException | GitAPIException e) {
            log.error("Failed to fetch changes", e);
            throw new RuntimeException("Failed to fetch changes", e);
        }
    }

    public static void cloneRepository(String remoteUrl, String localPath) {
        try {
            Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(new File(localPath))
                    .call();

            System.out.println("Cloned repository from " + remoteUrl + " to " + localPath);
        } catch (GitAPIException e) {
            System.err.println("Failed to clone repository: " + e.getMessage());
            throw new RuntimeException("Failed to clone repository", e);
        }
    }

    public static void cloneRepository(String remoteUrl, String localPath, @NotNull Log log) {
        try {
            Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(new File(localPath))
                    .call();

            log.info("Cloned repository from " + remoteUrl + " to " + localPath);
        } catch (GitAPIException e) {
            log.error("Failed to clone repository: " + e.getMessage());
            throw new RuntimeException("Failed to clone repository", e);
        }
    }
    public static void deleteBranch(String branchName) {
        try (Git git = Git.open(new File("."))) {
            git.branchDelete()
                    .setBranchNames(branchName)
                    .setForce(true)
                    .call();

            System.out.println("Deleted branch: " + branchName);
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to delete branch: " + e.getMessage());
            throw new RuntimeException("Failed to delete branch", e);
        }
    }

    public static void deleteBranch(String branchName, @NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            git.branchDelete()
                    .setBranchNames(branchName)
                    .setForce(true)
                    .call();

            log.info("Deleted branch: " + branchName);
        } catch (IOException | GitAPIException e) {
            log.error("Failed to delete branch: " + branchName, e);
            throw new RuntimeException("Failed to delete branch", e);
        }
    }   

    public static void deleteRemoteBranch(String branchName) {
        try (Git git = Git.open(new File("."))) {
            RefSpec refSpec = new RefSpec(":" + branchName);
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(refSpec)
                    .call();

            System.out.println("Deleted remote branch: " + branchName);
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to delete remote branch: " + e.getMessage());
            throw new RuntimeException("Failed to delete remote branch", e);
        }
    }

    public static void deleteRemoteBranch(String branchName, @NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            RefSpec refSpec = new RefSpec(":" + branchName);
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(refSpec)
                    .call();

            log.info("Deleted remote branch: " + branchName);
        } catch (IOException | GitAPIException e) {
            log.error("Failed to delete remote branch: " + branchName, e);
            throw new RuntimeException("Failed to delete remote branch", e);
        }
    }

    public static void deleteLocalTag(String tagName) {
        try (Git git = Git.open(new File("."))) {
            git.tagDelete()
                    .setTags(tagName)
                    .call();

            System.out.println("Deleted local tag: " + tagName);
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to delete local tag: " + e.getMessage());
            throw new RuntimeException("Failed to delete local tag", e);
        }
    }

    public static void deleteLocalTag(String tagName, @NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            git.tagDelete()
                    .setTags(tagName)
                    .call();

            log.info("Deleted local tag: " + tagName);
        } catch (IOException | GitAPIException e) {
            log.error("Failed to delete local tag: " + tagName, e);
            throw new RuntimeException("Failed to delete local tag", e);
        }
    }

    public static void deleteRemoteTag(String tagName) {
        try (Git git = Git.open(new File("."))) {
            RefSpec refSpec = new RefSpec(":" + tagName);
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(refSpec)
                    .call();

            System.out.println("Deleted remote tag: " + tagName);
        } catch (IOException | GitAPIException e) {
            System.err.println("Failed to delete remote tag: " + e.getMessage());
            throw new RuntimeException("Failed to delete remote tag", e);
        }
    }

    public static void deleteRemoteTag(String tagName, @NotNull Log log) {
        try (Git git = Git.open(new File("."))) {
            RefSpec refSpec = new RefSpec(":" + tagName);
            git.push()
                    .setRemote("origin")
                    .setRefSpecs(refSpec)
                    .call();

            log.info("Deleted remote tag: " + tagName);
        } catch (IOException | GitAPIException e) {
            log.error("Failed to delete remote tag: " + tagName, e);
            throw new RuntimeException("Failed to delete remote tag", e);
        }
    }
}