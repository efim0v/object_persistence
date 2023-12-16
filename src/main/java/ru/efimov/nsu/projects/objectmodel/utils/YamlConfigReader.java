package ru.efimov.nsu.projects.objectmodel.utils;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

public class YamlConfigReader {
    public static Map<String, Object> readConfig(String filename) {
        Yaml yaml = new Yaml();
        try (InputStream in = YamlConfigReader.class.getClassLoader().getResourceAsStream(filename)) {
            return yaml.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read YAML configuration", e);
        }
    }
}