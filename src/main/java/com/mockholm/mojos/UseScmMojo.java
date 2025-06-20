package com.mockholm.mojos;

import com.mockholm.utils.GitCredentialUtils;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.settings.Settings;

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

            if(GitCredentialUtils.isSSH(scm)){
                getLog().info("ssh");
            } else{
                getLog().info("credentials");
            }

            String serverKey=project.getProperties().getProperty("gitProvider");

            getLog().info("getProvider: "+serverKey);

            getLog().info("server password:"+ settings.getServer(serverKey).getPassword());

        }


        // You can now pass this to JGit

    }
}
