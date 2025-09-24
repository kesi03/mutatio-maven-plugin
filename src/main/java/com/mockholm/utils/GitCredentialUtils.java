package com.mockholm.utils;

import com.mockholm.config.GitConfiguration;
import org.apache.maven.model.Scm;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.transport.sshd.agent.connector.Factory;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.transport.sshd.KeyPasswordProvider;
import org.eclipse.jgit.transport.sshd.ServerKeyDatabase;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;
import org.eclipse.jgit.transport.sshd.agent.ConnectorFactory;
import org.eclipse.jgit.util.FS;
import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Utility class for handling Git authentication and repository connection details.
 * Supports both SSH and HTTPS configurations, derived from Maven SCM metadata.
 */
public class GitCredentialUtils {

    public static final String SSH_REMOTE = "ssh-remote";

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
        return transport -> {
            if (transport instanceof SshTransport) {
                ((SshTransport) transport).setSshSessionFactory(createSshSessionFactory(sshKeyPath));
            } else {
                // Optional: log a warning or fallback

            }
        };


    }

    /**
     * Creates a JGit {@link TransportConfigCallback} that configures the Git transport
     * layer to use SSH authentication with the specified private key and optional passphrase.
     *
     * @param sshKeyPath the file path to the private SSH key to use
     * @param passphrase the passphrase for the private key, or {@code null} if the key is unencrypted
     * @return the configured TransportConfigCallback for use with JGit operations
     */
    public static TransportConfigCallback getSSHCallBack(String sshKeyPath, String passphrase) {
        return transport -> {
            if (transport instanceof SshTransport) {
                ((SshTransport) transport).setSshSessionFactory(createSshSessionFactory(sshKeyPath, passphrase));
            } else {
                // Optional: log a warning or fallback
            }
        };
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

    /**
     * Creates a custom {@link JschConfigSessionFactory} that disables strict host key checking
     * and loads an identity file from the given path using an optional passphrase.
     * Used internally to support SSH transport.
     *
     * @param sshKeyPath the file path to the SSH private key
     * @param passphrase the passphrase for the private key, or {@code null} if unencrypted
     * @return a configured session factory for use with SSH Git operations
     */
    private static JschConfigSessionFactory createSshSessionFactory(String sshKeyPath, String passphrase) {
        return new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
                session.setConfig("IdentitiesOnly", "yes");
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch jsch = super.createDefaultJSch(fs);
                if (passphrase != null) {
                    jsch.addIdentity(sshKeyPath, passphrase);
                } else {
                    jsch.addIdentity(sshKeyPath);
                }
                return jsch;
            }
        };
    }

    /**
     * Converts a Git repository URL from HTTPS format to SSH format.
     * <p>
     * For example, converts:
     * {@code https://github.com/user/repo.git} to {@code git@github.com:user/repo.git}
     *
     * @param httpsUrl the HTTPS Git URL to convert
     * @return the equivalent SSH Git URL
     * @throws IllegalArgumentException if the input is null, not HTTPS, or malformed
     */
    public static String convertHttpsToSsh(String httpsUrl) {
        if (httpsUrl == null || !httpsUrl.startsWith("https://")) {
            throw new IllegalArgumentException("Invalid HTTPS Git URL: " + httpsUrl);
        }

        String stripped = httpsUrl.substring("https://".length());
        int slashIndex = stripped.indexOf('/');
        if (slashIndex == -1) {
            throw new IllegalArgumentException("Malformed Git URL: " + httpsUrl);
        }

        String host = stripped.substring(0, slashIndex);
        String path = stripped.substring(slashIndex + 1);

        return "git@" + host + ":" + path;
    }

    /**
     * Converts a Git repository URL from SSH format to HTTPS format.
     * <p>
     * For example, converts:
     * {@code git@github.com:user/repo.git} to {@code https://github.com/user/repo.git}
     *
     * @param sshUrl the SSH Git URL to convert
     * @return the equivalent HTTPS Git URL
     * @throws IllegalArgumentException if the input is null, not SSH, or malformed
     */
    public static String convertSshToHttps(String sshUrl) {
        if (sshUrl == null || !sshUrl.startsWith("git@") || !sshUrl.contains(":")) {
            throw new IllegalArgumentException("Invalid SSH Git URL: " + sshUrl);
        }

        String[] parts = sshUrl.split("@");
        if (parts.length != 2 || !parts[1].contains(":")) {
            throw new IllegalArgumentException("Malformed SSH Git URL: " + sshUrl);
        }

        String hostAndPath = parts[1];
        String[] hostPathSplit = hostAndPath.split(":", 2);
        String host = hostPathSplit[0];
        String path = hostPathSplit[1];

        return "https://" + host + "/" + path;
    }

    /**
     * Converts the 'origin' remote URL from HTTPS to SSH format using the Git config.
     *
     * @param git the Git instance
     * @return the SSH-formatted URL
     * @throws IllegalArgumentException if the remote URL is missing or invalid
     */
    public static String convertOriginHttpsToSsh(Git git) {
        String remoteUrl = git.getRepository()
                .getConfig()
                .getString("remote", "origin", "url");
        System.out.println("My Remote URL: " + remoteUrl);
        return convertHttpsToSsh(remoteUrl);
    }

    /**
     * Converts the 'origin' remote URL from SSH to HTTPS format using the Git config.
     *
     * @param git the Git instance
     * @return the HTTPS-formatted URL
     * @throws IllegalArgumentException if the remote URL is missing or invalid
     */
    public static String convertOriginSshToHttps(Git git) {
        String remoteUrl = git.getRepository()
                .getConfig()
                .getString("remote", "origin", "url");
        return convertSshToHttps(remoteUrl);
    }

    /**
     * Retrieves the URL of the specified remote from the Git configuration.
     * <p>
     * Equivalent to running {@code git config --get remote.origin.url}.
     *
     * @param git the Git instance
     * @param remoteName the name of the remote (e.g., "origin")
     * @return the remote URL, or {@code null} if not found
     */
    public static String getRemoteUrl(Git git, String remoteName) {
        return git.getRepository()
                .getConfig()
                .getString("remote", remoteName, "url");
    }

    /**
     * Retrieves the URL of the 'origin' remote from the Git configuration.
     *
     * @param git the Git instance
     * @return the remote URL for 'origin', or {@code null} if not found
     */
    public static String getRemoteUrl(Git git) {
        return getRemoteUrl(git, "origin");
    }


    /**
     * Adds a new SSH remote to the given Git repository using the SSH URL
     * converted from the existing HTTPS origin.
     *
     * @param git the Git repository instance
     * @throws URISyntaxException if the SSH URI is malformed
     * @throws GitAPIException if the remote addition fails
     */
    public static void addSSHRemote(Git git) throws URISyntaxException, GitAPIException {
        String origin = GitCredentialUtils.convertOriginHttpsToSsh(git);
        RemoteAddCommand remoteAddCommand = git.remoteAdd();
        remoteAddCommand.setName(SSH_REMOTE);
        remoteAddCommand.setUri(new URIish(origin));
        remoteAddCommand.call();
    }


    /**
     * Retrieves the configured server credentials from the provided Git configuration.
     *
     * @param configuration the Git configuration containing settings and server key
     * @return the Server configuration for the given key
     */
    public static Server getServer(GitConfiguration configuration) {
        return configuration.getSettings().getServer(configuration.getServerKey());
    }


    /**
     * Retrieves the configured server credentials from Maven settings.
     *
     * @param serverKey the ID of the server in settings.xml
     * @param settings the Maven settings object
     * @return the Server configuration for the given key
     */
    public static Server getServer(String serverKey, Settings settings) {
        return settings.getServer(serverKey);
    }

    /**
     * Builds an SshdSessionFactory using the server credentials from the provided Git configuration.
     *
     * @param configuration the Git configuration containing settings and server key
     * @return a configured SshdSessionFactory instance
     */
    public static SshdSessionFactory getSshdSessionFactory(GitConfiguration configuration) {
        Settings settings = configuration.getSettings();
        String serverKey = configuration.getServerKey();

        if (settings == null || serverKey == null || settings.getServer(serverKey) == null) {
            // TeamCity scenario or missing Maven settings
            System.out.println("Settings and ServerKey null");
            return getDefaultSshdSessionFactory();
        }
        return getSshdSessionFactory(getServer(configuration));
    }


    /**
     * Builds an SshdSessionFactory using the server credentials identified by the given key.
     *
     * @param serverKey the ID of the server in settings.xml
     * @param settings the Maven settings object
     * @return a configured SshdSessionFactory instance
     */
    public static SshdSessionFactory getSshdSessionFactory(String serverKey, Settings settings) {
        if (settings == null || serverKey == null || settings.getServer(serverKey) == null) {
            // TeamCity scenario or missing Maven settings
            return getDefaultSshdSessionFactory();
        }
        return getSshdSessionFactory(getServer(serverKey, settings));
    }

    /**
     * Gets a default SshdSessionFactory
     /**
     * Gets a default SshdSessionFactory with relaxed host key checking
     *
     * @return a configured SshdSessionFactory instance
     */
    public static SshdSessionFactory getDefaultSshdSessionFactory() {
        System.out.println("user.home = " + System.getProperty("user.home"));
        File sshDir = new File(System.getProperty("user.home"), ".ssh");
        System.out.println("SSH Directory: " + sshDir.getAbsolutePath());
        File knownHosts = new File(sshDir, "known_hosts");
        System.out.println("Known Hosts File: " + knownHosts);
        ServerKeyDatabase permissiveHostKeyDatabase = new ServerKeyDatabase() {
            @Override
            public List<PublicKey> lookup(String s, InetSocketAddress inetSocketAddress, Configuration configuration) {
                return List.of();
            }

            @Override
            public boolean accept(String s, InetSocketAddress inetSocketAddress, PublicKey publicKey, Configuration configuration, CredentialsProvider credentialsProvider) {
                return true;
            }


        };

        return new SshdSessionFactoryBuilder()
                .setPreferredAuthentications("publickey")
                .setHomeDirectory(new File(System.getProperty("user.home")))
                .setSshDirectory(sshDir)
                .setDefaultKnownHostsFiles(homeDir -> {
                    System.out.println("homeDir: " + homeDir);
                    Path knownHostsPath = new File(homeDir, "known_hosts").toPath();
                    return List.of(knownHostsPath);
                })
                .setServerKeyDatabase((homeDir, sshDirFile) -> permissiveHostKeyDatabase)
                .setConnectorFactory(new Factory())
                .build(null);
    }

    /**
     * Builds an SshdSessionFactory using the provided server credentials.
     * This factory supports passphrase-protected SSH keys.
     *
     * @param server the Maven server configuration containing the SSH passphrase
     * @return a configured SshdSessionFactory instance
     */
    public static SshdSessionFactory getSshdSessionFactory(Server server) {
        return new SshdSessionFactoryBuilder()
                .setHomeDirectory(new File(System.getProperty("user.home")))
                .setSshDirectory(new File(System.getProperty("user.home"), ".ssh"))
                .setKeyPasswordProvider(credentialsProvider -> new KeyPasswordProvider() {
                    private int attempts = 1;

                    @Override
                    public void setAttempts(int i) {
                        this.attempts = i;
                    }

                    @Override
                    public int getAttempts() {
                        return attempts;
                    }

                    @Override
                    public char[] getPassphrase(URIish uri, int attempt) {
                        return server.getPassphrase().toCharArray();
                    }

                    @Override
                    public boolean keyLoaded(URIish uri, int attempt, Exception error) {
                        return false;
                    }
                })
                .build(null);
    }
}