package com.mockholm.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.maven.plugin.logging.Log;

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

    public ShellCommand setAzureVariable(String name, String value) throws Exception {
        String template = "echo \"##vso[task.setvariable variable=%s;]%s\"";
        String commandString = String.format(template, name, value);
        runCommand(commandString);
        return this;
    }

    public ShellCommand setTeamCityParameter(String name, String value) throws Exception {
        String template = "echo \"##teamcity[setParameter name='%s' value='%s']\"";
        String commandString = String.format(template, name, value);
        runCommand(commandString);
        return this;
    }

    public ShellCommand setJenkinsVariable(String name, String value) throws Exception {
        String template = "echo \"export %s=%s\" >> $JENKINS_HOME/env-vars.properties";
        String commandString = String.format(template, name, value);
        runCommand(commandString);
        return this;
    }

    public ShellCommand setGitHubActionsVariable(String name, String value) throws Exception {
        String template = "echo \"%s=%s\" >> $GITHUB_ENV";
        String commandString = String.format(template, name, value);
        runCommand(commandString);
        return this;
    }

    public ShellCommand setDotEnv(String name,String value) throws Exception {
        setProperties(name, value,".env");
        return this;
    }

    public ShellCommand setProperties(String name,String value,String propertiesFile) throws Exception {
        String template = "echo \"%s=%s\" >> %s";
        String commandString = String.format(template, name, value,propertiesFile);
        runCommand(commandString);
        return this;
    }

    public ShellCommand run(String command) throws Exception{
        runCommand(command);
        return this;
    }

    private void runShellCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[] { ShellCommand.SHELL, "-c", command });
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            info(line);
        }
        process.waitFor();
    }

    private void runBashCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[] { ShellCommand.BASH, "-c", command });
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            info(line);
        }
        process.waitFor();
    }

    private void runPowerShellCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[] { ShellCommand.POWERSHELL, "-Command", command });
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            info(line);
        }
        process.waitFor();
    }

    private void runCommand(String command) throws Exception {
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

    private boolean isBashAvailable() {
        try {
            Process process = Runtime.getRuntime().exec(new String[] { "bash", "--version" });
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

}
