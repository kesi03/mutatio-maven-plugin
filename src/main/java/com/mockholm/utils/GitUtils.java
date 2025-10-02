package com.mockholm.utils;

import com.mockholm.config.FetchMode;
import com.mockholm.config.GitConfiguration;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.SshTransport;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static com.mockholm.utils.GitCredentialUtils.SSH_REMOTE;

/**
 * Utility class for performing common Git operations using JGit.
 */
public class GitUtils {
    /**
     * Retrieves the name of the current Git branch from the repository located in the current directory.
     *
     * @return the name of the active branch
     * @throws RuntimeException if the repository cannot be opened or accessed
     */
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

    /**
     * Retrieves the previous non-pre-release Git tag using a provided Git instance.
     *
     * @param git {@link Git}the JGit Git instance
     * @param log {@link Log}
     * @return the name of the previous valid tag, or null if none found
     * @throws RuntimeException if Git operations fail
     */
    public static String getPreviousTag(Git git,GitConfiguration configuration,Log log) {

        try {
            GitLogUtils.setLog(log);
            List<RefSpec> refSpecs = new ArrayList<>();

            refSpecs.add(new RefSpec("+refs/tags/*:refs/tags/*"));

            GitCredentialUtils.addSSHRemote(git);

            FetchCommand fetchCmd = git.fetch()
                    .setRemote(SSH_REMOTE)
                    .setRefSpecs(refSpecs);

            if (GitCredentialUtils.isSSH(configuration.getScm())) {
                GitLogUtils.info("SSH fetch tags");
                fetchCmd.setTransportConfigCallback(transport -> {
                    if (transport instanceof SshTransport) {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                    }
                });
            } else {
                GitLogUtils.info("HTTPS fetch tags");
                CredentialsProvider credentialsProvider =
                        GitCredentialUtils.getUserProvider(configuration.getSettings()
                                .getServer(configuration.getServerKey()).getPassword());
                fetchCmd.setCredentialsProvider(credentialsProvider);
            }

            fetchCmd.call();


            List<Ref> allTags = git.tagList().call();

            if (allTags.isEmpty()) {
                GitLogUtils.info("No tags found.");
                return null;
            }

            RevWalk revWalk = new RevWalk(git.getRepository());
            Map<RevCommit, String> tagMap = new HashMap<>();

            for (Ref tagRef : allTags) {
                String tagName = Repository.shortenRefName(tagRef.getName());

                // Skip pre-release identifiers: beta, alpha, rc, snapshot (case-insensitive)
                if (tagName.toLowerCase().matches(".*-(beta|alpha|rc|snapshot).*")) {
                    continue;
                }

                RevCommit commit = revWalk.parseCommit(tagRef.getObjectId());
                tagMap.put(commit, tagName);
            }

            if (tagMap.size() < 2) {
                GitLogUtils.info("Less than two non-pre-release tags available.");
                return null;
            }

            List<Map.Entry<RevCommit, String>> sortedTags = new ArrayList<>(tagMap.entrySet());
            sortedTags.sort((a, b) -> Integer.compare(b.getKey().getCommitTime(), a.getKey().getCommitTime()));

            String previousTag = sortedTags.get(1).getValue(); // second most recent
            GitLogUtils.info("Previous valid tag: " + previousTag);
            return previousTag;

        } catch (GitAPIException | IOException e) {
            GitLogUtils.error("Failed to retrieve previous valid Git tag", e);
            throw new RuntimeException("Unable to get previous valid tag", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void safeFetch(Git git, GitConfiguration configuration, Log log, FetchMode mode) {
        GitLogUtils.setLog(log);

        try {
            List<RefSpec> refSpecs = new ArrayList<>();

            switch (mode) {
                case BRANCHES:
                    refSpecs.add(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
                    break;
                case TAGS:
                    refSpecs.add(new RefSpec("+refs/tags/*:refs/tags/*"));
                    break;
                case BOTH:
                    refSpecs.add(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
                    refSpecs.add(new RefSpec("+refs/tags/*:refs/tags/*"));
                    break;
            }

            FetchCommand fetchCmd = git.fetch()
                    .setRemote("origin")
                    .setRefSpecs(refSpecs);

            if (GitCredentialUtils.isSSH(configuration.getScm())) {
                GitLogUtils.info("SSH fetch mode: " + mode);
                fetchCmd.setTransportConfigCallback(transport -> {
                    if (transport instanceof SshTransport) {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(GitCredentialUtils.getSshdSessionFactory(configuration));
                    }
                });
            } else {
                GitLogUtils.info("HTTPS fetch mode: " + mode);
                CredentialsProvider credentialsProvider =
                        GitCredentialUtils.getUserProvider(configuration.getSettings()
                                .getServer(configuration.getServerKey()).getPassword());
                fetchCmd.setCredentialsProvider(credentialsProvider);
            }

            fetchCmd.call();

        } catch (GitAPIException e) {
            GitLogUtils.warn("Fetch failed: " + e.getMessage());
        }
    }
}