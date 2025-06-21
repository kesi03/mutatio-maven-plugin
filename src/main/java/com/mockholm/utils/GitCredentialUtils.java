package com.mockholm.utils;

import org.apache.maven.model.Scm;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import com.jcraft.jsch.*;

/**
 * Utility class for handling Git authentication and repository connection details.
 * Supports both SSH and HTTPS configurations, derived from Maven SCM metadata.
 */
public class GitCredentialUtils {

    /**
     * Extracts the raw Git URL from the given Maven SCM object.
     * Assumes the connection string starts with "scm:git:".
     *
     * @param scm the Maven SCM object containing the connection string
     * @return the raw Git repository URL
     * @throws IllegalArgumentException if the SCM connection is null or improperly formatted
     */
    public static String getGitUrl(Scm scm){
        if (scm == null || !scm.getConnection().startsWith("scm:git:")) {
            throw new IllegalArgumentException("Invalid SCM format: " + scm);
        }

        return scm.getConnection().replaceFirst("^scm:git:", "");
    }

    /**
     * Determines whether the SCM connection uses an SSH-based Git URL.
     * Checks for common SSH prefixes such as "git@" or "ssh://".
     *
     * @param scm the Maven SCM object containing the connection string
     * @return {@code true} if the URL uses SSH; {@code false} otherwise
     * @throws IllegalArgumentException if the SCM connection is null or improperly formatted
     */
    public static boolean isSSH(Scm scm){
        if (scm == null || !scm.getConnection().startsWith("scm:git:")) {
            throw new IllegalArgumentException("Invalid SCM format: " + scm);
        }

        String gitUrl = scm.getConnection().replaceFirst("^scm:git:", "");

        return (gitUrl.startsWith("git@") || gitUrl.startsWith("ssh://"));
    }

    /**
     * Creates a JGit {@link TransportConfigCallback} that configures the Git transport
     * layer to use SSH authentication with the specified private key.
     *
     * @param sshKeyPath the file path to the private SSH key to use
     * @return the configured TransportConfigCallback for use with JGit operations
     */
    public static TransportConfigCallback getSSHCallBack(String sshKeyPath){
        return (TransportConfigCallback) transport ->
                ((SshTransport) transport).setSshSessionFactory(createSshSessionFactory(sshKeyPath));
    }

    /**
     * Creates a {@link UsernamePasswordCredentialsProvider} using a personal access token
     * for Git operations over HTTPS.
     *
     * @param token the personal access token (PAT) to use for authentication
     * @return a credentials provider configured with the token
     */
    public static UsernamePasswordCredentialsProvider getUserProvider(String token){
        return new UsernamePasswordCredentialsProvider(token, "");
    }

    /**
     * Creates a custom {@link JschConfigSessionFactory} that disables strict host key checking
     * and loads an identity file from the given path. Used internally to support SSH transport.
     *
     * @param sshKeyPath the file path to the SSH private key
     * @return a configured session factory for use with SSH Git operations
     */
    private static JschConfigSessionFactory createSshSessionFactory(String sshKeyPath) {
        return new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
                session.setConfig("IdentitiesOnly", "yes");
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch jsch = super.createDefaultJSch(fs);
                jsch.addIdentity(sshKeyPath);
                return jsch;
            }
        };
    }
}