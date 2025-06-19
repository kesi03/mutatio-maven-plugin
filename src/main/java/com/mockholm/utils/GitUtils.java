package com.mockholm.utils;

import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;

public class GitUtils {
    public static String getCurrentBranch(){
        String branch="";
        Git git = null;
        try {
            git = Git.open(new File("."));
            branch = git.getRepository().getBranch();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return branch;
    }
}
