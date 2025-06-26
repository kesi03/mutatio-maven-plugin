package com.mockholm.mojos;

import com.mockholm.utils.GitCredentialUtils;
import org.apache.maven.model.Scm;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.mockholm.utils.GitCredentialUtils.SSH_REMOTE;

@Mojo(name = "use-scm", defaultPhase = LifecyclePhase.VALIDATE)
public class UseScmMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter( defaultValue = "${settings}", readonly = true )
    private Settings settings;

    public void execute() {
        Scm scm= project.getScm();
        if(scm!=null){
            String connection = project.getScm().getConnection();
            String devConnection = project.getScm().getDeveloperConnection();
            String url = project.getScm().getUrl();

            getLog().info("SCM Connection: " + connection);
            getLog().info("SCM Developer Connection: " + devConnection);
            getLog().info("SCM URL: " + url);

            String serverKey=project.getProperties().getProperty("gitProvider");

            getLog().info("getProvider: "+serverKey);

            getLog().info("server passphrase:"+ settings.getServer(serverKey).getPassphrase());

            if(GitCredentialUtils.isSSH(scm)){
                getLog().info("ssh");
                SshdSessionFactory sshSessionFactory = GitCredentialUtils.getSshdSessionFactory(serverKey, settings);
                List<Ref> branches = getRefs(sshSessionFactory);
                branches.forEach(ref -> getLog().info("ref: "+ref.getName()));
                try {
                    pull(sshSessionFactory,getLog());
                } catch (IOException | GitAPIException e) {
                    throw new RuntimeException(e);
                }

            } else{
                getLog().info("credentials");
            }



        }


        // You can now pass this to JGit

    }


    private void pull(SshdSessionFactory sshSessionFactory, Log log) throws IOException, GitAPIException {

        try (Git git = Git.open(new File("."))) {
            Repository repo = git.getRepository();
            ObjectId oldHead = repo.resolve("HEAD^{tree}");

            GitCredentialUtils.addSSHRemote(git);

            PullResult result = git.pull()
                    .setRemote(SSH_REMOTE)
                    .setTransportConfigCallback(transport -> {
                        if (transport instanceof SshTransport) {
                            SshTransport sshTransport = (SshTransport) transport;
                            sshTransport.setSshSessionFactory(sshSessionFactory);
                        }
                    })
                    .call();

            MergeResult merge = result.getMergeResult();
            if (merge == null || !merge.getMergeStatus().isSuccessful()) {
                log.info("No merge occurred or merge was not successful.");
                return;
            }

            ObjectId newHead = repo.resolve("HEAD^{tree}");
            if (Objects.equals(oldHead, newHead)) {
                log.info("Pull completed: repository already up to date. No changes.");
                return;
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
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Ref> getRefs(SshdSessionFactory sshSessionFactory) {
        TransportConfigCallback transportConfigCallback = transport -> {
            if (transport instanceof SshTransport) {
                ((SshTransport) transport).setSshSessionFactory(sshSessionFactory);
            }
        };

        Supplier<List<Ref>> listBranches = () -> {
            try {
                return Git.open(new File(".")).branchList().call();
            } catch (IOException | GitAPIException e) {
                throw new RuntimeException("Failed to list branches", e);
            }
        };

        return listBranches.get();
    }


}
