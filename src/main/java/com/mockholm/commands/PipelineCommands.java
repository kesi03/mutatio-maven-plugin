package com.mockholm.commands;

import org.apache.maven.plugin.logging.Log;
import okhttp3.*;

import java.io.IOException;

/**
 * Builder command used to send api calls to pipelines
 */
public class PipelineCommands {
    private final Log log;

    /**
     *
     * @param log {@link Log}
     */
    public PipelineCommands(Log log) {
        this.log = log;
    }

    /**
     * Logs an informational message using the configured logger,
     * or prints to standard output if no logger is available.
     *
     * @param msg the message to log
     */
    private void info(String msg) {
        if (log != null)
            log.info(msg);
        else
            System.out.println(msg);
    }

    /**
     * Logs a warning message using the configured logger,
     * or prints with a "WARN:" prefix to standard output if no logger is available.
     *
     * @param msg the warning message to log
     */
    @SuppressWarnings("unused")
    private void warn(String msg) {
        if (log != null)
            log.warn(msg);
        else
            System.out.println("WARN: " + msg);
    }

    /**
     * Logs an error message and associated throwable using the configured logger,
     * or prints to standard error if no logger is available.
     *
     * @param msg the error message to log
     * @param t   the throwable to include in the log output
     */
    private void error(String msg, Throwable t) {
        if (log != null)
            log.error(msg, t);
        else {
            System.err.println("ERROR: " + msg);
            t.printStackTrace(System.err);
        }
    }


    /**
     * Updates a parameter in a TeamCity project via the REST API using OkHttp.
     *
     * @param teamCityUrl The base URL of your TeamCity server (e.g., "https://teamcity.example.com")
     * @param projectId   The ID of the TeamCity project where the parameter should be updated
     * @param paramName   The name of the parameter to update
     * @param paramValue  The new value to assign to the parameter
     * @param authToken   A Bearer token used for authentication (alternatively, use Basic Auth)
     * @return            The current instance of PipelineCommands for chaining
     * @throws IOException If there is a problem communicating with the TeamCity API
     */
    public PipelineCommands updateTeamCityParameter(
            String teamCityUrl,
            String projectId,
            String paramName,
            String paramValue,
            String authToken
    ) throws IOException {

        OkHttpClient client = new OkHttpClient();

        String url = teamCityUrl + "/app/rest/projects/id:" + projectId + "/parameters/" + paramName;
        String xmlBody = "<property name=\"" + paramName + "\" value=\"" + paramValue + "\"/>";

        RequestBody body = RequestBody.create(xmlBody, MediaType.parse("application/xml"));
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + authToken) // or Basic auth header
                .addHeader("Accept", "application/xml")
                .addHeader("Content-Type", "application/xml")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                info("TeamCity parameter updated: " + paramName + " = " + paramValue);
            } else {
                warn("Update failed: " + response.code());
                warn("Response: " + response.body().string());
            }
        }
        return this;
    }

    /**
     * Updates a variable in an Azure DevOps Variable Group via the REST API using OkHttp.
     *
     * @param orgUrl           The base URL of the Azure DevOps organization (e.g., "https://dev.azure.com/myOrg")
     * @param project          The project name within Azure DevOps
     * @param variableGroupId  The numeric ID of the variable group to update
     * @param variableName     The name of the variable to update
     * @param newValue         The new value to assign to the variable
     * @param patToken         A Personal Access Token (PAT) with access to modify variable groups
     * @return                 The current instance of PipelineCommands for chaining
     * @throws IOException     If there is a problem communicating with the Azure DevOps API
     */
    public PipelineCommands updateAzureDevOpsVariable(
            String orgUrl,            // e.g., https://dev.azure.com/myOrg
            String project,
            String variableGroupId,   // numeric ID of the variable group
            String variableName,
            String newValue,
            String patToken           // Personal Access Token
    ) throws IOException {

        OkHttpClient client = new OkHttpClient();

        String apiUrl = String.format("%s/%s/_apis/distributedtask/variablegroups/%s?api-version=7.1-preview.2",
                orgUrl, project, variableGroupId);

        // Compose the JSON body
        String jsonBody = "{ \"variables\": { \"" + variableName + "\": { \"value\": \"" + newValue + "\" } } }";

        // Auth header: Basic with PAT
        String authHeader = Credentials.basic("", patToken);

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .put(body)
                .addHeader("Authorization", authHeader)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                info("Updated Azure DevOps variable: " + variableName);
            } else {
                warn("Failed to update variable. HTTP status: " + response.code());
                warn("Response body: " + response.body().string());
            }
        }
        return this;
    }

    /**
     * Updates a GitHub Actions repository variable using the GitHub REST API and OkHttp.
     *
     * @param repoOwner     The owner of the GitHub repository (e.g. "my-org" or "my-username")
     * @param repoName      The name of the GitHub repository
     * @param variableName  The name of the GitHub Actions variable to update
     * @param variableValue The new value to assign to the variable
     * @param githubToken   The GitHub personal access token with appropriate repository scopes
     * @throws IOException  If an error occurs while performing the HTTP request
     * @return {@link PipelineCommands} as part of a build chain
     */
    public PipelineCommands updateGitHubActionsVariable(
            String repoOwner,      // e.g., "my-org" or "my-username"
            String repoName,       // e.g., "my-repo"
            String variableName,   // e.g., "MY_ENV_VAR"
            String variableValue,  // e.g., "new_value_123"
            String githubToken     // GitHub Personal Access Token (PAT)
    ) throws IOException {

        OkHttpClient client = new OkHttpClient();

        String url = String.format(
                "https://api.github.com/repos/%s/%s/actions/variables/%s",
                repoOwner, repoName, variableName
        );

        String jsonBody = String.format("{ \"name\": \"%s\", \"value\": \"%s\" }", variableName, variableValue);
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + githubToken)
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("X-GitHub-Api-Version", "2022-11-28") // optional but recommended
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
               info("Updated GitHub Actions variable: " + variableName);
            } else {
                warn("Failed to update variable. HTTP " + response.code());
                warn("Response: " + response.body().string());
            }
        }
        return this;
    }

}
