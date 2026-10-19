package com.harukiyu.clarityeconomy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class YamlValidationTest {

    @Test
    public void testAllYamlFilesAreValid() throws Exception {
        Path resourcesPath = Paths.get("src", "main", "resources");
        Yaml yaml = new Yaml();
        try (Stream<Path> paths = Files.walk(resourcesPath)) {
            List<Path> ymlFiles = paths.filter(p -> p.toString().endsWith(".yml")).toList();
            Assertions.assertFalse(ymlFiles.isEmpty(), "No YAML files found in resources directory!");
            for (Path path : ymlFiles) {
                try (InputStream in = Files.newInputStream(path)) {
                    Object parsed = yaml.load(in);
                    Assertions.assertNotNull(parsed, "Failed to parse YAML file: " + path);
                    Assertions.assertInstanceOf(Map.class, parsed, "Root element of " + path + " must be a Map!");

                    if (path.toString().contains("languages")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) parsed;

                        // Ensure dialog section exists
                        Assertions.assertTrue(map.containsKey("dialog"), "Missing 'dialog' section in " + path);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> dialog = (Map<String, Object>) map.get("dialog");

                        // Ensure market, blackmarket, jobs, jobs_guide exist in dialog (verifying no duplicate section overwrites)
                        Assertions.assertTrue(dialog.containsKey("market"), "Missing 'dialog.market' in " + path);
                        Assertions.assertTrue(dialog.containsKey("blackmarket"), "Missing 'dialog.blackmarket' in " + path);
                        Assertions.assertTrue(dialog.containsKey("promosi"), "Missing 'dialog.promosi' in " + path);
                        Assertions.assertTrue(dialog.containsKey("jobs"), "Missing 'dialog.jobs' in " + path);
                        Assertions.assertTrue(dialog.containsKey("jobs_guide"), "Missing 'dialog.jobs_guide' in " + path);

                        @SuppressWarnings("unchecked")
                        Map<String, Object> jobs = (Map<String, Object>) dialog.get("jobs");
                        Assertions.assertTrue(jobs.containsKey("no_job_title"), "Missing 'dialog.jobs.no_job_title' in " + path);
                        Assertions.assertTrue(jobs.containsKey("no_job_body"), "Missing 'dialog.jobs.no_job_body' in " + path);
                        Assertions.assertTrue(jobs.containsKey("btn_choose_now"), "Missing 'dialog.jobs.btn_choose_now' in " + path);
                        Assertions.assertTrue(jobs.containsKey("btn_guide"), "Missing 'dialog.jobs.btn_guide' in " + path);
                        Assertions.assertTrue(jobs.containsKey("btn_main_menu"), "Missing 'dialog.jobs.btn_main_menu' in " + path);

                        // Verify titles have <white> and do not have <bold> or plugin name
                        String jobsTitle = (String) jobs.get("title");
                        Assertions.assertTrue(jobsTitle.startsWith("<white>") && jobsTitle.endsWith("</white>"), "dialog.jobs.title must be <white>...: " + jobsTitle);
                        Assertions.assertFalse(jobsTitle.contains("<bold>"), "dialog.jobs.title must not be bold: " + jobsTitle);
                        Assertions.assertFalse(jobsTitle.toLowerCase().contains("clarityeconomy"), "dialog.jobs.title must not contain plugin name: " + jobsTitle);

                        String noJobTitle = (String) jobs.get("no_job_title");
                        Assertions.assertTrue(noJobTitle.startsWith("<white>") && noJobTitle.endsWith("</white>"), "dialog.jobs.no_job_title must be <white>...: " + noJobTitle);
                        Assertions.assertFalse(noJobTitle.contains("<bold>"), "dialog.jobs.no_job_title must not be bold: " + noJobTitle);
                        Assertions.assertFalse(noJobTitle.toLowerCase().contains("clarityeconomy"), "dialog.jobs.no_job_title must not contain plugin name: " + noJobTitle);
                    }
                } catch (Exception e) {
                    Assertions.fail("Syntax or assertion error in YAML file: " + path + " -> " + e.getMessage());
                }
            }
        }
    }
}
