package org.example;

import java.util.HashSet;
import java.util.Set;

/**
 * Example configuration POJO.
 * <p>
 * Configurations are saved and loaded to JSON files
 * <p>
 * All fields should be public and mutable.
 * <p>
 * Fields to static inner classes generate nested JSON objects.
 */
public class AutoIgnoreHardConfig {

    public final AutoIgnoreHardModule autoIgnoreHardModule = new AutoIgnoreHardModule();

    public static class AutoIgnoreHardModule {
        public boolean enabled = true;
        public boolean logToFile = true;
        public boolean logToChat = false;
        public Set<String> allowList = new HashSet<>();
    }
}
