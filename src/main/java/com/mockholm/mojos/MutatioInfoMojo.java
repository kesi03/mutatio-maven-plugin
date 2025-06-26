package com.mockholm.mojos;

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
import com.mockholm.utils.GitUtils;
import com.mockholm.utils.SemanticVersion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * This Mojo is used to provide information about the current Mutatio release.
 * It is typically called to display the next patch version based on the current version.
 */
@Mojo(name = "mutatio-info", requiresDirectInvocation = true,
requiresProject = true, aggregator = true
)
public class MutatioInfoMojo extends AbstractMojo
{
    /**
     * The Maven project being built.
     * This is used to access project properties and configuration.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * The type of release being performed.
     * This is used to determine the next version based on the current version.
     */
    // Default is PATCH, but can be set to MAJOR or MINOR as needed.
    // This allows flexibility in versioning based on the type of changes made.
    // For example, a MAJOR release would increment the major version number,
    // while a MINOR release would increment the minor version number.
    // PATCH is typically used for bug fixes and minor changes.
    // The releaseType can be set via the command line using the -DreleaseType=<type> option.
    // Valid values are: PATCH, MINOR, MAJOR.
    // The default value is PATCH, which means the next version will be a patch version.
    // Example usage: mvn mutatio-info -DreleaseType=MINOR
    // This will set the release type to MINOR, and the next version will be incremented accordingly.
    // If no releaseType is specified, it defaults to PATCH.
    // This allows for easy customization of the release process based on the type of changes made.
    @Parameter(name="releaseType", property = "releaseType", defaultValue = "PATCH" , required = true, readonly = false)
    private ReleaseType releaseType;

    /**
     * The version identifier used to specify the version of the release.
     * This can be used to set a specific version or leave it empty to use the current version.
     * If not specified, the current version of the project will be used.
     * This allows for flexibility in versioning, enabling users to specify a custom version if needed.
     * Example usage: mvn mutatio-info -DversionIdentifier=1.0.0
     * This will set the version identifier to 1.0.0, and the next version will be calculated based on this identifier.
     */
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
