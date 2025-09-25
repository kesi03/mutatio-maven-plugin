package com.mockholm.mojos.commons;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.jetbrains.annotations.NotNull;

import com.mockholm.commands.GitCommand;
import com.mockholm.commands.ShellCommand;
import com.mockholm.config.BranchType;
import com.mockholm.config.GitConfiguration;
import com.mockholm.models.ConventionalCommit;
import com.mockholm.models.MojoCommons;
import com.mockholm.utils.CommitUtils;
import com.mockholm.utils.GitUtils;
import com.mockholm.utils.SemanticVersion;

/**
 * This class provides common functionality for handling dependencies in Maven projects.
 * It includes methods to collate artifacts and update dependencies based on a release version.
 */
public class DependencyMojoCommons {
    private final MojoCommons commons;

    /**
     * Constructs a DependencyMojoCommons instance with the provided MojoCommons.
     *
     * @param commons the MojoCommons instance containing project and settings information
     */
    public DependencyMojoCommons(MojoCommons commons) {
        this.commons = commons;
    }

    /**
     * Collates artifacts for a given release version and branch type.
     *
     * @param release        the release version to collate artifacts for
     * @param mainOrMaster   the branch type (main or master) to collate artifacts from
     * @throws IOException if an I/O error occurs while reading or writing files
     */
    public void collateArtifacts(@NotNull String release, BranchType mainOrMaster) throws IOException {
        commons.getLog()
                .info("collate artifacts for release: " + release + ", mainOrMaster: " + mainOrMaster.getValue());
        commons.getLog().info("currentBranch: " + GitUtils.getCurrentBranch());
        commons.getLog().info("Current version: " + commons.getProject().getVersion());

        SemanticVersion currentVersion = SemanticVersion.parse(commons.getProject().getVersion());

        commons.getLog().info("Current version: " + currentVersion.toString());

        SemanticVersion releaseVersion = SemanticVersion.parse(release);

        commons.getLog().info("Release version: " + releaseVersion.toString());

        String releaseBranch = commons.getReleaseBranch()+"/" + releaseVersion.toString();

        String releaseTag = commons.getReleaseBranch()+"-" + releaseVersion.toString();

        String baseDir = commons.getProject().getBasedir().getAbsolutePath();

        commons.getLog().info("mainOrMaster: " + mainOrMaster.getValue());

        GitConfiguration gitConfiguration = new GitConfiguration()
                .withServerKey(commons.getProject().getProperties().getProperty("gitProvider"))
                .withScm(commons.getProject().getScm())
                .withSettings(commons.getSettings());

        new GitCommand(commons.getLog())
                .changeBranch(releaseBranch, gitConfiguration)
                .gitInfo()
                .runShellCommands(cmd -> {
                    try {
                        ProjectBuildingRequest buildingRequest = commons.getSession().getProjectBuildingRequest();
                        buildingRequest.setResolveDependencies(true);

                        MavenProject rootProject = commons.getProjectBuilder()
                                .build(new File(baseDir, "pom.xml"), buildingRequest)
                                .getProject();

                        Set<String> artifactNames = new LinkedHashSet<>();
                        collectArtifactsRecursively(rootProject, artifactNames);

                        commons.getLog().info("Artifacts in branch '" + releaseBranch + "':");

                        artifactNames.forEach(a -> {
                            commons.getLog().info(" - " + a);
                        });

                        // create a string with the artifacts
                        String artifactsString = String.join(";", artifactNames);

                        List<String[]> properties = Arrays.asList(
                                new String[] { "MUTATIO_RELEASE_BRANCH", releaseBranch },
                                new String[] { "MUTATIO_RELEASE_TAG", releaseTag },
                                new String[] { "MUTATIO_RELEASE_ARTIFACTS", artifactsString },
                                new String[] { "MUTATIO_RELEASE_VERSION", releaseVersion.toString() });
                        cmd.setBuildProperties(properties);

                    } catch (ProjectBuildingException e) {
                        commons.getLog().error("Error building project: " + e.getMessage());
                        e.printStackTrace();
                    } catch (Exception e) {
                        commons.getLog().error("Error collecting artifacts: " + e.getMessage());
                        e.printStackTrace();
                    }

                }, new ShellCommand(commons.getLog()))
                .close();

    }

    /**
     * Updates dependencies in the release branch based on the provided release version and branch type.
     *
     * @param release        the release version to update dependencies for
     * @param mainOrMaster   the branch type (main or master) to update dependencies in
     * @param artifacts      a semicolon-separated string of artifact coordinates to update
     * @throws IOException if an I/O error occurs while reading or writing files
     */
    public void updateDependencies(@NotNull String release, BranchType mainOrMaster, String artifacts)
            throws IOException {
        commons.getLog().info(
                "updateDependencies called with release: " + release + ", mainOrMaster: " + mainOrMaster.getValue());

        SemanticVersion releaseVersion = SemanticVersion.parse(release);
        String releaseBranch = commons.getReleaseBranch()+"/" + releaseVersion.toString();

        commons.getLog().info("Switching to branch: " + releaseBranch);
        GitConfiguration gitConfiguration = new GitConfiguration()
                .withServerKey(commons.getProject().getProperties().getProperty("gitProvider"))
                .withScm(commons.getProject().getScm())
                .withSettings(commons.getSettings());

        AtomicReference<String> commitMessage = new AtomicReference<>("");

        new GitCommand(commons.getLog())
                .changeBranch(releaseBranch, gitConfiguration)
                .gitInfo()
                .runShellCommands(cmd -> {
                    try {
                        ProjectBuildingRequest buildingRequest = commons.getSession().getProjectBuildingRequest();
                        buildingRequest.setResolveDependencies(true);

                        MavenProject rootProject = commons.getProjectBuilder()
                                .build(new File(commons.getProject().getBasedir(), "pom.xml"), buildingRequest)
                                .getProject();

                        Set<String> artifactKeys = Arrays.stream(artifacts.split(";"))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toCollection(LinkedHashSet::new));
                        updateDependenciesRecursively(rootProject, artifactKeys);

                        commons.getLog().info("Updated dependencies in branch '" + releaseBranch + "'");

                        String description = artifactKeys.stream()
                                .map(key -> "Updated dependency: " + key)
                                .collect(Collectors.joining("\n"));

                        ConventionalCommit commit = new ConventionalCommit.Builder()
                                .type(BranchType.RELEASE)
                                .scope(releaseBranch)
                                .description(description)
                                .isBreaking(false)
                                .body("")
                                .footer("")
                                .build();

                        commitMessage.set(CommitUtils.format(commit));
                        commons.getLog().info("Commit: " + commitMessage);

                        List<String[]> properties = Arrays.asList(
                                new String[] { "MUTATIO_UPDATED_DEPENDENCIES", artifacts },
                                new String[] { "MUTATIO_DEPENDENCIES_UPDATED", "true" });
                        cmd.setBuildProperties(properties);

                    } catch (Exception e) {
                        commons.getLog().error("Error updating dependencies: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, new ShellCommand(commons.getLog()))
                .addAllChanges()
                .commit(commitMessage.get())
                .push(gitConfiguration)
                .close();
    }

    /**
     * Recursively collects all artifacts from the given Maven project and its
     * submodules.
     *
     * @param project   the Maven project to process
     * @param artifacts a set to collect artifact coordinates in the format
     *                  {@code groupId:artifactId:version}
     * @throws Exception if reading or writing the POM files fails
     */
    private void collectArtifactsRecursively(MavenProject project, Set<String> artifacts) throws Exception {
        artifacts.add(project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());

        for (String module : project.getModules()) {
            File modulePom = new File(project.getBasedir(), module + "/pom.xml");
            MavenProject subProject = commons.getProjectBuilder()
                    .build(modulePom, commons.getSession().getProjectBuildingRequest()).getProject();
            collectArtifactsRecursively(subProject, artifacts);
        }
    }

    /**
     * Recursively updates the versions of dependencies in the given Maven project
     * and its submodules
     * based on a provided set of artifact coordinates in the format
     * {@code groupId:artifactId:version}.
     *
     * @param project      the Maven project to process
     * @param artifactKeys a set of artifact coordinates to match and update, in the
     *                     format {@code groupId:artifactId:version}
     * @throws Exception if reading or writing the POM files fails
     */
    private void updateDependenciesRecursively(MavenProject project, Set<String> artifactKeys) throws Exception {
        File pomFile = new File(project.getBasedir(), "pom.xml");
        Model model = new MavenXpp3Reader().read(new FileReader(pomFile));

        // Build a map of groupId:artifactId -> version for quick lookup
        Map<String, String> artifactVersionMap = artifactKeys.stream()
                .map(String::trim)
                .filter(s -> s.split(":").length == 3)
                .collect(Collectors.toMap(
                        s -> s.split(":")[0] + ":" + s.split(":")[1],
                        s -> s.split(":")[2],
                        (v1, v2) -> v1, // keep first if duplicates
                        LinkedHashMap::new));

        boolean modified = false;
        for (Dependency dep : model.getDependencies()) {
            String key = dep.getGroupId() + ":" + dep.getArtifactId();
            if (artifactVersionMap.containsKey(key)) {
                String newVersion = artifactVersionMap.get(key);
                if (!newVersion.equals(dep.getVersion())) {
                    dep.setVersion(newVersion);
                    commons.getLog().info("Updated dependency: " + key +
                            " to version " + newVersion + " in " + pomFile.getAbsolutePath());
                    modified = true;
                }
            }
        }

        if (modified) {
            try (FileWriter writer = new FileWriter(pomFile)) {
                new MavenXpp3Writer().write(writer, model);
            }
        }

        // Recurse into submodules
        for (String module : project.getModules()) {
            File modulePom = new File(project.getBasedir(), module + "/pom.xml");
            MavenProject subProject = commons.getProjectBuilder()
                    .build(modulePom, commons.getSession().getProjectBuildingRequest()).getProject();
            updateDependenciesRecursively(subProject, artifactKeys);
        }
    }

}
