package br.com.sosviale.offline;

import br.com.sosviale.offline.dto.OfflineSessionDto;
import br.com.sosviale.offline.dto.OfflineSnapshot;
import br.com.sosviale.offline.dto.PendingOperationDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Persistência local em JSON (~/.sos-viale/offline/).
 */
public class OfflineStore {

    /** ROOT deve ser declarado antes de INSTANCE (ordem de init estática em Java). */
    private static final Path ROOT = resolveRootPath();
    private static final OfflineStore INSTANCE = new OfflineStore();

    private static Path resolveRootPath() {
        String home = System.getProperty("user.home");
        if (home == null || home.isBlank()) {
            home = System.getProperty("user.dir", ".");
        }
        return Path.of(home, ".sos-viale", "offline");
    }

    private OfflineStore() {
        try {
            Files.createDirectories(ROOT);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível criar pasta offline: " + e.getMessage(), e);
        }
    }

    public static OfflineStore getInstance() {
        return INSTANCE;
    }

    public void saveSnapshot(String usuario, OfflineSnapshot snapshot) throws IOException {
        snapshot.setUsuario(usuario);
        write(usuarioDir(usuario).resolve("snapshot.json"), snapshot);
    }

    public Optional<OfflineSnapshot> loadSnapshot(String usuario) {
        return read(usuarioDir(usuario).resolve("snapshot.json"), OfflineSnapshot.class);
    }

    public void saveSession(OfflineSessionDto session) throws IOException {
        write(usuarioDir(session.getUsuario()).resolve("session.json"), session);
    }

    public Optional<OfflineSessionDto> loadSession(String usuario) {
        return read(usuarioDir(usuario).resolve("session.json"), OfflineSessionDto.class);
    }

    public Optional<OfflineSessionDto> loadAnySession() {
        if (!Files.isDirectory(ROOT)) return Optional.empty();
        try (Stream<Path> dirs = Files.list(ROOT)) {
            return dirs.filter(Files::isDirectory)
                    .map(dir -> loadSession(dir.getFileName().toString()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public boolean hasSnapshot(String usuario) {
        return Files.isRegularFile(usuarioDir(usuario).resolve("snapshot.json"));
    }

    public boolean hasAnySnapshot() {
        if (!Files.isDirectory(ROOT)) return false;
        try (Stream<Path> dirs = Files.list(ROOT)) {
            return dirs.anyMatch(dir ->
                    Files.isRegularFile(dir.resolve("snapshot.json")));
        } catch (IOException e) {
            return false;
        }
    }

    public List<PendingOperationDto> loadPending(String usuario) {
        return readList(usuarioDir(usuario).resolve("pending.json"), PendingOperationDto.class);
    }

    public void savePending(String usuario, List<PendingOperationDto> pending) throws IOException {
        write(usuarioDir(usuario).resolve("pending.json"), pending);
    }

    private Path usuarioDir(String usuario) {
        String safe = usuario.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path dir = ROOT.resolve(safe);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return dir;
    }

    private void write(Path file, Object value) throws IOException {
        OfflineJson.MAPPER.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), value);
    }

    private <T> Optional<T> read(Path file, Class<T> type) {
        if (!Files.isRegularFile(file)) return Optional.empty();
        try {
            return Optional.of(OfflineJson.MAPPER.readValue(file.toFile(), type));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private <T> List<T> readList(Path file, Class<T> elementType) {
        if (!Files.isRegularFile(file)) return new ArrayList<>();
        try {
            return OfflineJson.MAPPER.readerForListOf(elementType).readValue(file.toFile());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}
