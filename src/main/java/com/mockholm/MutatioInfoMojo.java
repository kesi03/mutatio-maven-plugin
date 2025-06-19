package com.mockholm;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.mockholm.config.ReleaseType;
import com.mockholm.config.VersionIdentifier;
import com.mockholm.mojos.FeatStartMojo;
import com.mockholm.utils.GitUtils;
import com.mockholm.utils.SemanticVersion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * 
 * @phase mutatio-info
 */
@Mojo(name = "mutatio-info", defaultPhase = LifecyclePhase.COMPILE)
public class MutatioInfoMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(name="releaseType", property = "releaseType", defaultValue = "PATCH" , required = true, readonly = false)
    private ReleaseType releaseType;

    @Parameter(name="versionIdentifier", property = "versionIdentifier", defaultValue = "" , required = false, readonly = false)
    private VersionIdentifier versionIdentifier;

    public MavenProject getProject() {
        return project;
    }

    public ReleaseType getReleaseType() {
        return releaseType;
    }

    public VersionIdentifier getVersionIdentifier() {
        return versionIdentifier;
    }

    public void execute() throws MojoExecutionException {
        getLog().info("currentBranch: "+ GitUtils.getCurrentBranch());
        getLog().info("versionIdentifier: "+versionIdentifier);
        getLog().info("releaseType: "+releaseType);
        getLog().info("Current version: " + project.getVersion());
        SemanticVersion currentVersion= SemanticVersion.parse(project.getVersion());
        SemanticVersion nextPatch = new SemanticVersion(currentVersion.getMajor(),currentVersion.getMinor(),currentVersion.getPatch()+1, currentVersion.getPreRelease(), currentVersion.getBuild());
        getLog().info("Next Patch version: " + nextPatch.toString());
    }
}
