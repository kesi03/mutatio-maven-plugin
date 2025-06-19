package com.mockholm.utils;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PomUtils {

    public static String getVersion(File pomFile) {
        try (FileReader reader = new FileReader(pomFile)) {
            MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            Model model = xpp3Reader.read(reader);
            return model.getVersion();
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException("Failed to read version from pom.xml: " + pomFile.getAbsolutePath(), e);
        }
    }

    public static String getVersion(String baseDir) {
        return getVersion(new File(baseDir, "pom.xml"));
    }
}
