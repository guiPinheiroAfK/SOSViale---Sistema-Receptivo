package br.com.sosviale.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

// .env opcional perto da pasta de trabalho; SO ganha sempre de arquivo

public final class EnvLoader {

    private static final Map<String, String> fromFile = new HashMap<>();
    private static volatile boolean loaded;

    private EnvLoader() {}

    public static void load() {
        if (loaded) {
            return;
        }
        synchronized (EnvLoader.class) {
            if (loaded) {
                return;
            }
            Path envFile = locateEnvFile();
            if (envFile != null) {
                parseEnvFile(envFile);
            }
            loaded = true;
        }
    }

    // primeiro env do SO depois arquivo; null se nem um tiver

    public static String get(String key) {
        load();
        String os = System.getenv(key);
        if (os != null && !os.isBlank()) {
            return os.trim();
        }
        return fromFile.get(key);
    }

    // sobe pela arvore procurando .env desde user.dir

    private static Path locateEnvFile() {
        Path start = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        for (Path dir = start; dir != null; dir = dir.getParent()) {
            Path candidate = dir.resolve(".env");
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            if (dir.getParent() == null || dir.getParent().equals(dir)) {
                break;
            }
        }
        return null;
    }

    private static void parseEnvFile(Path envFile) {
        try {
            for (String line : Files.readAllLines(envFile, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (!key.isEmpty() && !value.isEmpty()) {
                    fromFile.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("[EnvLoader] Não foi possível ler " + envFile + ": " + e.getMessage());
        }
    }
}
