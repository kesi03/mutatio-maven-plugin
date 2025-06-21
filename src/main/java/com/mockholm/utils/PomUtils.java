package com.mockholm.utils;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class for reading Maven project metadata from a {@code pom.xml} file.
 * Provides methods to extract the project version from a given file or directory.
 */
public class PomUtils {

    /**
     * Reads and returns the version defined in the given {@code pom.xml} file.
     *
     * @param pomFile the {@code pom.xml} file to parse
     * @return the project version declared in the POM
     * @throws RuntimeException if the file cannot be read or parsed
     */
    public static String getVersion(File pomFile) {
        try (FileReader reader = new FileReader(pomFile)) {
            MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            Model model = xpp3Reader.read(reader);
            return model.getVersion();
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException("Failed to read version from pom.xml: " + pomFile.getAbsolutePath(), e);
        }
    }

    /**
     * Locates a {@code pom.xml} file in the specified base directory and extracts the version.
     *
     * @param baseDir the directory containing the {@code pom.xml}
     * @return the project version declared in the POM
     * @throws RuntimeException if the file is missing, unreadable, or unparsable
     */
    public static String getVersion(String baseDir) {
        return getVersion(new File(baseDir, "pom.xml"));
    }
}
