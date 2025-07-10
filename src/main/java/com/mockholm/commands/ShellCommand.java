package com.mockholm.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

import com.mockholm.config.BuildSystem;

/**
 * Utility class for executing shell commands and setting environment variables
 * across different CI/CD platforms including Azure DevOps, GitHub Actions,
 * Jenkins, TeamCity, and fallback support for .env files.
 */
public class ShellCommand {
    private final Log log;
    public static String BASH = "/bin/bash";
    public static String SHELL = "/bin/sh";
    public static String POWERSHELL = "powershell.exe";

    public ShellCommand(Log log) {
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
     * Sets a variable in Azure DevOps using the task.setvariable command.
     *
     * @param name  the name of the variable
     * @param value the value to assign
     * @return      the current ShellCommand instance for chaining
     * @throws Exception if command execution fails
     */
    public ShellCommand setAzureVariable(String name, String value) throws Exception {
        String template = "echo \"##vso[task.setvariable variable=%s;]%s\"";
        String commandString = String.format(template, name, value);
        runCommand(commandString);
        return this;
    }

    /**
     * Sets a parameter in TeamCity using the service message syntax.
     *
     * @param name  the parameter name
     * @param value the value to assign
     * @return      the current ShellCommand instance for chaining
     * @throws Exception if command execution fails
     */
    public ShellCommand setTeamCityParameter(String name, String value) throws Exception {
        String template = "echo \"##teamcity[setParameter name='%s' value='%s']\"";
        String commandString = String.format(template, name, value);
        runCommand(commandString);
        return this;
    }

    /**
     * Appends an environment variable to Jenkins' env-vars.properties file.
     *
     * @param name  the variable name
     * @param value the value to assign
     * @return      the current ShellCommand instance for chaining
     * @throws Exception if command execution fails
     */
    public ShellCommand setJenkinsVariable(String name, String value) throws Exception {
        String template = "echo \"export %s=%s\" >> $JENKINS_HOME/env-vars.properties";
        String commandString = String.format(template, name, value);
        runCommand(commandString);
        return this;
    }

    /**
     * Appends an environment variable to the GitHub Actions environment file.
     *
     * @param name  the variable name
     * @param value the value to assign
     * @return      the current ShellCommand instance for chaining
     * @throws Exception if command execution fails
     */
    public ShellCommand setGitHubActionsVariable(String name, String value) throws Exception {
        String template = "echo \"%s=%s\" >> $GITHUB_ENV";
        String commandString = String.format(template, name, value);
        runCommand(commandString);
        return this;
    }

    /**
     * Writes a variable into the default .env file.
     *
     * @param name  the variable name
     * @param value the value to assign
     * @return      the current ShellCommand instance for chaining
     * @throws Exception if command execution fails
     */
    public ShellCommand setDotEnv(String name, String value) throws Exception {
        return setProperties(name, value, ".env");
    }

    /**
     * Writes a variable into a specified properties file.
     *
     * @param name           the variable name
     * @param value          the value to assign
     * @param propertiesFile the file to append to
     * @return               the current ShellCommand instance for chaining
     * @throws Exception if command execution fails
     */
    public ShellCommand setProperties(String name, String value, String propertiesFile) throws Exception {
        String template = "echo \"%s=%s\" >> %s";
        String commandString = String.format(template, name, value, propertiesFile);
        runCommand(commandString);
        return this;
    }

    /**
     * Sets multiple build properties based on the detected CI/CD platform.
     *
     * @param properties a list of name-value pairs
     * @return           the current ShellCommand instance for chaining
     */
    public ShellCommand setBuildProperties(List<String[]> properties) {
        properties.forEach(pair -> {
            try {
                setBuildProperty(pair[0], pair[1], ShellCommand.getCICDPlatform());
            } catch (Exception e) {
                error("setBuildProperties: error",e);
            }
        });
        return this;
    }

    /**
     * Sets a single build property for the specified build system.
     *
     * @param name  the property name
     * @param value the value to assign
     * @param bs    the target BuildSystem
     * @return      the current ShellCommand instance for chaining
     * @throws Exception if command execution fails
     */
    public ShellCommand setBuildProperty(String name, String value, BuildSystem bs) throws Exception {
        switch(bs){
            case AZURE_DEVOPS:
                return setAzureVariable(name, value);
            case GITHUB_ACTIONS:
                return setGitHubActionsVariable(name, value);
            case JENKINS:
                return setJenkinsVariable(name,value);
            case TEAM_CITY:
                return setTeamCityParameter(name, value);
            case UNKNOWN:
                return setDotEnv(name, value);
            default:
                return setDotEnv(name, value);
        }
    }

    /**
     * Executes a shell command using the appropriate interpreter for the OS.
     *
     * @param command the shell command to execute
     * @return        the current ShellCommand instance for chaining
     * @throws Exception if execution fails or OS is unsupported
     */
    public ShellCommand run(String command) throws Exception {
        runCommand(command);
        return this;
    }

    /**
     * Detects the current CI/CD platform based on environment variables.
     *
     * @return the detected BuildSystem enum value
     */
    public static BuildSystem getCICDPlatform() {
        if (System.getenv("TF_BUILD") != null) {
            return BuildSystem.AZURE_DEVOPS;
        } else if (System.getenv("TEAMCITY_VERSION") != null) {
            return BuildSystem.TEAM_CITY;
        } else if (System.getenv("JENKINS_URL") != null) {
            return BuildSystem.JENKINS;
        } else if ("true".equals(System.getenv("GITHUB_ACTIONS"))) {
            return BuildSystem.GITHUB_ACTIONS;
        } else {
            return BuildSystem.UNKNOWN;
        }
    }

    /**
     * Executes a shell command using the system's default shell (`/bin/sh`)
     * and logs each line of standard output.
     *
     * @param command the command to execute
     * @throws Exception if an I/O error occurs or the process is interrupted
     */
    public void runShellCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[] { ShellCommand.SHELL, "-c", command });
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            info(line);
        }
        process.waitFor();
    }

    /**
     * Executes a shell command using the Bash shell (`/bin/bash`)
     * and logs each line of standard output.
     *
     * @param command the command to execute
     * @throws Exception if an I/O error occurs or the process is interrupted
     */
    public void runBashCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[] { ShellCommand.BASH, "-c", command });
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            info(line);
        }
        process.waitFor();
    }

    /**
     * Executes a command using PowerShell (`powershell.exe`) on Windows systems
     * and logs each line of standard output.
     *
     * @param command the command to execute
     * @throws Exception if an I/O error occurs or the process is interrupted
     */
    public void runPowerShellCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[] { ShellCommand.POWERSHELL, "-Command", command });
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            info(line);
        }
        process.waitFor();
    }

    /**
     * Determines the current operating system and executes the command using
     * the appropriate shell interpreter (PowerShell for Windows, Bash or default shell for Unix-based systems).
     *
     * @param command the command to execute
     * @throws Exception if the OS is unsupported or command execution fails
     */
    public void runCommand(String command) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            runPowerShellCommand(command);
        } else if (os.contains("mac") || os.contains("nix") || os.contains("nux")) {
            if (isBashAvailable()) {
                runBashCommand(command);
            } else {
                runShellCommand(command);
            }
        } else {
            error("Unsupported operating system: ",
                    new UnsupportedOperationException("Unsupported operating system: " + os));
            throw new UnsupportedOperationException("Unsupported operating system: " + os);
        }
    }

    /**
     * Checks if Bash is available on the system by running `bash --version`.
     *
     * @return true if Bash is available, false otherwise
     */
    public boolean isBashAvailable() {
        try {
            Process process = Runtime.getRuntime().exec(new String[] { "bash", "--version" });
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

}
