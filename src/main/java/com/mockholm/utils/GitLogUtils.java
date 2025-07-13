package com.mockholm.utils;

import org.apache.maven.plugin.logging.Log;

/**
 * Maven-compatible logging utility for Git-related output.
 * Falls back to console if Log isn't initialized.
 */
public class GitLogUtils {

    private static Log log;

    public static void setLog(Log mavenLog) {
        log = mavenLog;
    }

    public static void info(String msg) {
        if (log != null) log.info(msg);
        else System.out.println(msg);
    }

    public static void warn(String msg) {
        if (log != null) log.warn(msg);
        else System.out.println("WARN: " + msg);
    }

    public static void error(String msg, Throwable t) {
        if (log != null) log.error(msg, t);
        else {
            System.err.println("ERROR: " + msg);
            t.printStackTrace(System.err);
        }
    }
}
